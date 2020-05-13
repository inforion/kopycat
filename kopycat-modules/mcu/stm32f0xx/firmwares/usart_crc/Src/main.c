/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2020 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <stream.h>
#include "tinyprintf.h"
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
UART_HandleTypeDef huart1;
UART_HandleTypeDef huart2;

/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_USART1_UART_Init(void);
static void MX_USART2_UART_Init(void);
/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */
static void uart_putf(void *unused, char c) {
    HAL_StatusTypeDef status;
    do {
        status = HAL_UART_Transmit(&huart1, (uint8_t*)&c, 1, 1000);
    } while (status == HAL_BUSY);
}

static void uart_printf_enter(void) {

}

static void uart_printf_exit(void) {

}

uint32_t crc32_for_byte(uint32_t r) {
    for(int j = 0; j < 8; ++j)
        r = (r & 1? 0: (uint32_t)0xEDB88320L) ^ r >> 1;
    return r ^ (uint32_t)0xFF000000L;
}

void crc32(const void *data, size_t n_bytes, uint32_t* crc) {
    static uint32_t table[0x100];
    if(!*table)
        for(size_t i = 0; i < 0x100; ++i)
            table[i] = crc32_for_byte(i);
    for(size_t i = 0; i < n_bytes; ++i)
        *crc = table[(uint8_t)*crc ^ ((uint8_t*)data)[i]] ^ *crc >> 8;
}

int message_read_buf(stream_t* stream, uint8_t* data) {
    // TODO: BUG HERE DoS
    size_t len = stream->ops->get_byte(stream, 3);
    // TODO: BUG HERE StackBOF
    stream->ops->to_buffer(stream, data, len, 4, 0);
    return len;
}

enum COMMAND {
    CMD_CRC32_ROUND = 0xE0,
    CMD_SEED_RST,
    CMD_SEED_ZERO,
    CMD_CRC32_DUMP,
    CMD_CRC32_GET,
    CMD_CRC32_DBG,
    CMD_SEED_INC,
    CMD_SEED_DEC
};

void process_message(stream_t* stream) {
    static uint32_t crc = 0xDEADBEEF;

    uint8_t data[16] = { 0 };

    uint8_t magick0 = stream->ops->get_byte(stream, 0);
    uint8_t magick1 = stream->ops->get_byte(stream, 1);

    if (magick0 == 0xC0 && magick1 == 0xBA) {

        // TODO: BUG HERE DoS
        uint8_t cmd = stream->ops->get_byte(stream, 2);

        switch (cmd) {
            case CMD_CRC32_ROUND: {
                int len = message_read_buf(stream, data);
                crc32(data, len, &crc);
                break;
            }

            case CMD_SEED_RST: {
                crc = 0xDEADBEEF;
                break;
            }

            case CMD_SEED_ZERO: {
                crc = 0;
                break;
            }

            case CMD_CRC32_DUMP: {
                int len = message_read_buf(stream, data);
                printf("Dump received data:\n");
                for (int k = 0; k < len; k++)
                    printf("%02X ", data[k]);
                printf("\n");
                break;
            }

            case CMD_CRC32_GET: {
                char response[13] = { 0 };
                sprintf(response + 0, "x%02X", (crc >> 24) & 0xFF);
                sprintf(response + 3, "x%02X", (crc >> 16) & 0xFF);
                sprintf(response + 6, "x%02X", (crc >>  8) & 0xFF);
                sprintf(response + 9, "x%02X", (crc >>  0) & 0xFF);
                response[12] = 'm';
                assert(HAL_OK == HAL_UART_Transmit(&huart2, (uint8_t*) response, 13, 5000));
                break;
            }

            case CMD_CRC32_DBG: {
                printf("debug = %08lX\n", crc);
                break;
            }

            case CMD_SEED_INC: {
                crc += 1;
                break;
            }

            case CMD_SEED_DEC: {
                crc -= 1;
                break;
            }

            default: {
                printf("Unknown command: %02X\n", cmd);
                break;
            }
        }
    } else {
        printf("Received unknown packet start marker: %02X %02X -> bail out!\n", magick0, magick1);
    }
}

uint8_t convert_octet(char ch) {
    if (ch >= 'A' && ch <= 'F')
        return ch - 'A' + 0xA;

    if (ch >= 'a' && ch <= 'f')
        return ch - 'a' + 0xA;

    if (ch >= '0' && ch <= '9')
        return ch - '0';

    return 0xFF;
}

enum STATE { STATE_IDLE = 0x10, STATE_BYTE, STATE_OCTET1, STATE_OCTET2, STATE_MESSAGE, STATE_SHRINK, STATE_RESET };
enum ACTION { ACTION_CONTINUE, ACTION_DONE, ACTION_ERROR };

int sm_message_parser(char ch, stream_t* stream) {
    static uint8_t tmp = 0;
    static int state = STATE_IDLE;

    switch (state) {
        case STATE_IDLE: {
            switch (ch) {
                case 'x': {
                    state = STATE_BYTE;
                    return ACTION_CONTINUE;
                }
                case 'r': {
                    state = STATE_RESET;
                    return ACTION_CONTINUE;
                }
                case 'm': {
                    state = STATE_MESSAGE;
                    return ACTION_CONTINUE;
                }
                case 's': {
                    state = STATE_SHRINK;
                    return ACTION_CONTINUE;
                }
                default: {
                    printf("Unknown message command value: '%c'\n", ch);
                    state = STATE_IDLE;
                    return ACTION_DONE;
                }
            }
        }

        case STATE_BYTE: {
            tmp = 0;
            state = STATE_OCTET1;
            return ACTION_DONE;
        }

        case STATE_OCTET1: {
            uint8_t octet = convert_octet(ch);
            if (octet != 0xFF) {
                tmp |= octet << 4;
                state = STATE_OCTET2;
            } else {
                state = STATE_IDLE;
            }
            return ACTION_DONE;
        }

        case STATE_OCTET2: {
            uint8_t octet = convert_octet(ch);
            if (octet != 0xFF) {
                tmp |= octet << 0;
                stream->ops->add(stream, tmp);
            }
            state = STATE_IDLE;
            return ACTION_DONE;
        }

        case STATE_RESET: {
            stream->ops->reset(stream);
            return ACTION_DONE;
        }

        case STATE_SHRINK: {
            stream->ops->shrink(stream);
            return ACTION_DONE;
        }

        case STATE_MESSAGE: {
            process_message(stream);
            stream->ops->reset(stream);
            state = STATE_IDLE;
            return ACTION_DONE;
        }

        default: {
//            printf("Unknown message parser state: %08X\n", state);
            state = STATE_IDLE;
            return ACTION_DONE;
        }
    }
}

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{
  /* USER CODE BEGIN 1 */
    init_printf(NULL, uart_putf, uart_printf_enter, uart_printf_exit);
  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

//    xC0xBAxE0x02xAAxBBmxC0xBAxE4m       CMD_CRC32_ROUND
//    xC0xBAxE0x02xCCxDDmxC0xBAxE4m       CMD_CRC32_ROUND
//    xC0xBAxE0x40xCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxCCxDDxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAxAAm       CMD_CRC32_ROUND
//    xC0xBAxE5m                          CMD_CRC32_GET
//    xC0xBAxE3x02xAAxBBm                 CMD_CRC32_DUMP

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_USART1_UART_Init();
  MX_USART2_UART_Init();
  /* USER CODE BEGIN 2 */
    printf("Initialization done\n");
  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
    HAL_StatusTypeDef status;
    stream_t* stream = new_stream();
  while (1)
  {
      char ch = 0;
      status = HAL_UART_Receive(&huart2, (uint8_t*)&ch, 1, HAL_MAX_DELAY);
      if (status == HAL_TIMEOUT)
          continue;

      assert(HAL_OK == HAL_UART_Transmit(&huart2, (uint8_t*)&ch, 1, 1000));

      int action;
      do {
          action = sm_message_parser(ch, stream);
      } while (action == ACTION_CONTINUE);

      if (action == ACTION_ERROR) {
          stream->ops->destroy(stream);
          Error_Handler();
      }

    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
  }
#pragma clang diagnostic pop

  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};
  RCC_PeriphCLKInitTypeDef PeriphClkInit = {0};

  /** Initializes the CPU, AHB and APB busses clocks 
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB busses clocks 
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
  PeriphClkInit.PeriphClockSelection = RCC_PERIPHCLK_USART1;
  PeriphClkInit.Usart1ClockSelection = RCC_USART1CLKSOURCE_PCLK1;
  if (HAL_RCCEx_PeriphCLKConfig(&PeriphClkInit) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief USART1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART1_UART_Init(void)
{

  /* USER CODE BEGIN USART1_Init 0 */

  /* USER CODE END USART1_Init 0 */

  /* USER CODE BEGIN USART1_Init 1 */

  /* USER CODE END USART1_Init 1 */
  huart1.Instance = USART1;
  huart1.Init.BaudRate = 115200;
  huart1.Init.WordLength = UART_WORDLENGTH_8B;
  huart1.Init.StopBits = UART_STOPBITS_1;
  huart1.Init.Parity = UART_PARITY_NONE;
  huart1.Init.Mode = UART_MODE_TX_RX;
  huart1.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart1.Init.OverSampling = UART_OVERSAMPLING_16;
  huart1.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart1.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_HalfDuplex_Init(&huart1) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART1_Init 2 */

  /* USER CODE END USART1_Init 2 */

}

/**
  * @brief USART2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART2_UART_Init(void)
{

  /* USER CODE BEGIN USART2_Init 0 */

  /* USER CODE END USART2_Init 0 */

  /* USER CODE BEGIN USART2_Init 1 */

  /* USER CODE END USART2_Init 1 */
  huart2.Instance = USART2;
  huart2.Init.BaudRate = 115200;
  huart2.Init.WordLength = UART_WORDLENGTH_8B;
  huart2.Init.StopBits = UART_STOPBITS_1;
  huart2.Init.Parity = UART_PARITY_NONE;
  huart2.Init.Mode = UART_MODE_TX_RX;
  huart2.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart2.Init.OverSampling = UART_OVERSAMPLING_16;
  huart2.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart2.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_UART_Init(&huart2) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART2_Init 2 */

  /* USER CODE END USART2_Init 2 */

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOA_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(LED_GPIO_Port, LED_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pin : LED_Pin */
  GPIO_InitStruct.Pin = LED_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(LED_GPIO_Port, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  printf("Critical error occurred, can't continue...\n");
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{ 
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     tex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/

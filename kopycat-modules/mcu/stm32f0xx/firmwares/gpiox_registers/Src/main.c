
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  ** This notice applies to any and all portions of this file
  * that are not between comment pairs USER CODE BEGIN and
  * USER CODE END. Other portions of this file, whether 
  * inserted by the user or by software development tools
  * are owned by their respective copyright owners.
  *
  * COPYRIGHT(c) 2020 STMicroelectronics
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *   1. Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *   2. Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation
  *      and/or other materials provided with the distribution.
  *   3. Neither the name of STMicroelectronics nor the names of its contributors
  *      may be used to endorse or promote products derived from this software
  *      without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  ******************************************************************************
  */
/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "stm32f0xx_hal.h"

/* USER CODE BEGIN Includes */
#include "assert.h"
#include "stdio.h"
#include <stdlib.h>
/* USER CODE END Includes */

/* Private variables ---------------------------------------------------------*/
UART_HandleTypeDef huart1;

/* USER CODE BEGIN PV */
/* Private variables ---------------------------------------------------------*/
char string[80];
int tests_count = 0;
int tests_failed = 0;
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_USART1_UART_Init(void);

/* USER CODE BEGIN PFP */
/* Private function prototypes -----------------------------------------------*/

/* USER CODE END PFP */

/* USER CODE BEGIN 0 */

int _write(int fd, char * ptr, int len) {
    HAL_UART_Transmit(&huart1, (uint8_t *) ptr, len, HAL_MAX_DELAY);
    return len;
}

int __io_putchar(int ch) {
    HAL_UART_Transmit(&huart1, (uint8_t *) &ch, 1, HAL_MAX_DELAY);
    return ch;
}

int test_GPIOx_MODER_s(void) {
    GPIOA->MODER = 0x000F000F;
    mu_assert_equals("MODER set", GPIOA->MODER, 0x000F000F);
}
int test_GPIOx_MODER_r(void) {
    GPIOA->MODER = 0x00000000;
    mu_assert_equals("MODER reset", GPIOA->MODER, 0x00000000);
}

int test_GPIOx_OTYPER_s(void) {
    GPIOA->OTYPER = 0x0000000F;
    mu_assert_equals("OTYPER set", GPIOA->OTYPER, 0x0000000F);
}
int test_GPIOx_OTYPER_r(void) {
    GPIOA->OTYPER = 0x00000000;
    mu_assert_equals("OTYPER reset", GPIOA->OTYPER, 0x00000000);
}

int test_GPIOx_OSPEEDR_s(void) {
    GPIOA->OSPEEDR = 0x000F000F;
    mu_assert_equals("OSPEEDR set", GPIOA->OSPEEDR, 0x000F000F);
}
int test_GPIOx_OSPEEDR_r(void) {
    GPIOA->OSPEEDR = 0x00000000;
    mu_assert_equals("OSPEEDR reset", GPIOA->OSPEEDR, 0x00000000);
}

int test_GPIOx_PUPDR_s(void) {
    GPIOA->PUPDR = 0x000F000F;
    mu_assert_equals("PUPDR set", GPIOA->PUPDR, 0x000F000F);
}
int test_GPIOx_PUPDR_r(void) {
    GPIOA->PUPDR = 0x00000000;
    mu_assert_equals("PUPDR reset", GPIOA->PUPDR, 0x00000000);
}

int test_GPIOx_IDR(void) {
    mu_assert_equals("IDR must be zero", GPIOA->IDR, 0x00000000);
}

int test_GPIOx_ODR_s(void) {
    GPIOA->ODR = 0x0000000F;
    mu_assert_equals("ODR set", GPIOA->ODR, 0x0000000F);
}
int test_GPIOx_ODR_r(void) {
    GPIOA->ODR = 0x00000000;
    mu_assert_equals("ODR reset", GPIOA->ODR, 0x00000000);
}

int test_GPIOx_BSRR_s(void) {
    GPIOA->ODR = 0x00000009;
    GPIOA->BSRR = 0x00000006;

    int ret = 0;
    mu_assert_equals_ret("BSRR should set ODR", GPIOA->ODR, 0x0000000F);
    mu_assert_equals_ret("BSRR must keep zero value", GPIOA->BSRR, 0x00000000);
    return ret;
}

int test_GPIOx_BSRR_r(void) {
    GPIOA->ODR = 0x0000000F;
    GPIOA->BSRR = 0x00090000;

    int ret = 0;
    mu_assert_equals_ret("BSRR should reset ODR", GPIOA->ODR, 0x00000006);
    mu_assert_equals_ret("BSRR must keep zero value", GPIOA->BSRR, 0x00000000);
    return ret;
}

int test_GPIOx_LCKR(void) {

    GPIOA->MODER = 0x00000001;
    GPIOA->OTYPER = 0x00000001;
    GPIOA->OSPEEDR = 0x00000001;
    GPIOA->PUPDR = 0x00000001;
    GPIOA->AFR[0] = 0x00000001;
    GPIOA->AFR[1] = 0x00000001;

    HAL_GPIO_LockPin(GPIOA, GPIO_PIN_0);

    GPIOA->MODER = 0x00000000;
    GPIOA->OTYPER = 0x00000000;
    GPIOA->OSPEEDR = 0x00000000;
    GPIOA->PUPDR = 0x00000000;
    GPIOA->AFR[0] = 0x00000000;
    GPIOA->AFR[1] = 0x00000000;

    int ret = 0;
    mu_assert_equals_ret("LCKR should lock MODER", GPIOA->MODER, 0x00000001);
    mu_assert_equals_ret("LCKR should lock OTYPER", GPIOA->OTYPER, 0x00000001);
    mu_assert_equals_ret("LCKR should lock OSPEEDR", GPIOA->OSPEEDR, 0x00000001);
    mu_assert_equals_ret("LCKR should lock PUPDR", GPIOA->PUPDR, 0x00000001);
    mu_assert_equals_ret("LCKR should lock AFR[0]", GPIOA->AFR[0], 0x00000001);
    mu_assert_equals_ret("LCKR should lock AFR[1]", GPIOA->AFR[1], 0x00000001);
    return ret;
}

int test_GPIOx_AFR(void) {
    mu_assert_equals("AFR[1]", GPIOA->AFR[0], 0x00000001);
    mu_assert_equals("AFR[2]", GPIOA->AFR[1], 0x00000001);
}

int test_GPIOx_BRR(void) {
    GPIOA->ODR = 0x0000000F;
    GPIOA->BRR = 0x00000009;
    mu_assert_equals("BRR should reset ODR", GPIOA->ODR, 0x00000006);
}

int all_tests(void) {
    mu_run_test(test_GPIOx_MODER_s);
    mu_run_test(test_GPIOx_MODER_r);
    mu_run_test(test_GPIOx_OTYPER_s);
    mu_run_test(test_GPIOx_OTYPER_r);
    mu_run_test(test_GPIOx_OSPEEDR_s);
    mu_run_test(test_GPIOx_OSPEEDR_r);
    mu_run_test(test_GPIOx_PUPDR_s);
    mu_run_test(test_GPIOx_PUPDR_r);
    mu_run_test(test_GPIOx_IDR);
    mu_run_test(test_GPIOx_ODR_s);
    mu_run_test(test_GPIOx_ODR_r);
    mu_run_test(test_GPIOx_BSRR_s);
    mu_run_test(test_GPIOx_BSRR_r);
    mu_run_test(test_GPIOx_LCKR);
    mu_run_test(test_GPIOx_AFR);
    mu_run_test(test_GPIOx_BRR);

    return tests_failed;
}

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  *
  * @retval None
  */
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration----------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_USART1_UART_Init();
  /* USER CODE BEGIN 2 */

  all_tests();

  printf("  failed: %i\r\n  done: %i\r\n  all: %i\r\n", tests_failed, tests_count - tests_failed, tests_count);

  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
	while (1) {

  /* USER CODE END WHILE */

  /* USER CODE BEGIN 3 */

		HAL_Delay(1000);

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

  RCC_OscInitTypeDef RCC_OscInitStruct;
  RCC_ClkInitTypeDef RCC_ClkInitStruct;
  RCC_PeriphCLKInitTypeDef PeriphClkInit;

    /**Initializes the CPU, AHB and APB busses clocks 
    */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = 16;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    _Error_Handler(__FILE__, __LINE__);
  }

    /**Initializes the CPU, AHB and APB busses clocks 
    */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    _Error_Handler(__FILE__, __LINE__);
  }

  PeriphClkInit.PeriphClockSelection = RCC_PERIPHCLK_USART1;
  PeriphClkInit.Usart1ClockSelection = RCC_USART1CLKSOURCE_PCLK1;
  if (HAL_RCCEx_PeriphCLKConfig(&PeriphClkInit) != HAL_OK)
  {
    _Error_Handler(__FILE__, __LINE__);
  }

    /**Configure the Systick interrupt time 
    */
  HAL_SYSTICK_Config(HAL_RCC_GetHCLKFreq()/1000);

    /**Configure the Systick 
    */
  HAL_SYSTICK_CLKSourceConfig(SYSTICK_CLKSOURCE_HCLK);

  /* SysTick_IRQn interrupt configuration */
  HAL_NVIC_SetPriority(SysTick_IRQn, 0, 0);
}

/* USART1 init function */
static void MX_USART1_UART_Init(void)
{

  huart1.Instance = USART1;
  huart1.Init.BaudRate = 38400;
  huart1.Init.WordLength = UART_WORDLENGTH_8B;
  huart1.Init.StopBits = UART_STOPBITS_1;
  huart1.Init.Parity = UART_PARITY_NONE;
  huart1.Init.Mode = UART_MODE_TX_RX;
  huart1.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart1.Init.OverSampling = UART_OVERSAMPLING_16;
  huart1.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart1.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_UART_Init(&huart1) != HAL_OK)
  {
    _Error_Handler(__FILE__, __LINE__);
  }

}

/** Configure pins
*/
static void MX_GPIO_Init(void)
{

  GPIO_InitTypeDef GPIO_InitStruct;

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOA_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOA, GPIO_PIN_0, GPIO_PIN_RESET);

  /*Configure GPIO pin : PA0 */
  GPIO_InitStruct.Pin = GPIO_PIN_0;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @param  file: The file name as string.
  * @param  line: The line in file as a number.
  * @retval None
  */
void _Error_Handler(char *file, int line)
{
  /* USER CODE BEGIN Error_Handler_Debug */
	/* User can add his own implementation to report the HAL error return state */
	while (1) {
	}
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
void assert_failed(uint8_t* file, uint32_t line)
{ 
  /* USER CODE BEGIN 6 */
	/* User can add his own implementation to report the file name and line number,
	 tex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/**
  * @}
  */

/**
  * @}
  */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/

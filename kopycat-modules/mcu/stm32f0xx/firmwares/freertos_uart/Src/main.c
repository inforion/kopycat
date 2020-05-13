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
  * This software component is licensed by ST under Ultimate Liberty license
  * SLA0044, the "License"; You may not use this file except in compliance with
  * the License. You may obtain a copy of the License at:
  *                             www.st.com/SLA0044
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "cmsis_os.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
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

osThreadId starterTaskHandle;
uint32_t starterTaskBuffer[ 128 ];
osStaticThreadDef_t starterTaskControlBlock;
osThreadId uartTaskHandle;
uint32_t uartTaskBuffer[ 128 ];
osStaticThreadDef_t uartTaskControlBlock;
osThreadId processTaskHandle;
uint32_t processTaskBuffer[ 128 ];
osStaticThreadDef_t processTaskControlBlock;
osThreadId watchDogTaskHandle;
uint32_t watchDogBuffer[ 128 ];
osStaticThreadDef_t watchDogControlBlock;
/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_USART1_UART_Init(void);
void StarterTaskHandler(void const * argument);
void UartTaskHandler(void const * argument);
void ProcessTaskHandler(void const * argument);
void WatchDogHandler(void const * argument);

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
    osThreadSuspendAll();
}

static void uart_printf_exit(void) {
    osThreadResumeAll();
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

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_USART1_UART_Init();
  /* USER CODE BEGIN 2 */

  /* USER CODE END 2 */

  /* USER CODE BEGIN RTOS_MUTEX */
  /* add mutexes, ... */
  /* USER CODE END RTOS_MUTEX */

  /* USER CODE BEGIN RTOS_SEMAPHORES */
  /* add semaphores, ... */
  /* USER CODE END RTOS_SEMAPHORES */

  /* USER CODE BEGIN RTOS_TIMERS */
  /* start timers, add new ones, ... */
  /* USER CODE END RTOS_TIMERS */

  /* USER CODE BEGIN RTOS_QUEUES */
  /* add queues, ... */
  /* USER CODE END RTOS_QUEUES */

  /* Create the thread(s) */
  /* definition and creation of starterTask */
  osThreadStaticDef(starterTask, StarterTaskHandler, osPriorityNormal, 0, 128, starterTaskBuffer, &starterTaskControlBlock);
  starterTaskHandle = osThreadCreate(osThread(starterTask), NULL);

  /* definition and creation of uartTask */
  osThreadStaticDef(uartTask, UartTaskHandler, osPriorityNormal, 0, 128, uartTaskBuffer, &uartTaskControlBlock);
  uartTaskHandle = osThreadCreate(osThread(uartTask), NULL);

  /* definition and creation of processTask */
  osThreadStaticDef(processTask, ProcessTaskHandler, osPriorityNormal, 0, 128, processTaskBuffer, &processTaskControlBlock);
  processTaskHandle = osThreadCreate(osThread(processTask), NULL);

  /* definition and creation of watchDogTask */
  osThreadStaticDef(watchDogTask, WatchDogHandler, osPriorityNormal, 0, 128, watchDogBuffer, &watchDogControlBlock);
  watchDogTaskHandle = osThreadCreate(osThread(watchDogTask), NULL);

  /* USER CODE BEGIN RTOS_THREADS */
  /* add threads, ... */
  /* USER CODE END RTOS_THREADS */

  /* Start scheduler */
  osKernelStart();
 
  /* We should never get here as control is now taken by the scheduler */
  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
  }
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
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI48;
  RCC_OscInitStruct.HSI48State = RCC_HSI48_ON;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB busses clocks 
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI48;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_1) != HAL_OK)
  {
    Error_Handler();
  }
  PeriphClkInit.PeriphClockSelection = RCC_PERIPHCLK_USART1;
  PeriphClkInit.Usart1ClockSelection = RCC_USART1CLKSOURCE_SYSCLK;
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
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOA_CLK_ENABLE();

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/* USER CODE BEGIN Header_StarterTaskHandler */
/**
  * @brief  Function implementing the starterTask thread.
  * @param  argument: Not used 
  * @retval None
  */
/* USER CODE END Header_StarterTaskHandler */
void StarterTaskHandler(void const * argument)
{
  /* USER CODE BEGIN 5 */
    printf("StarterTaskHandler start!\n");
  /* Infinite loop */
  for(int k = 0; k < 128; k++) {
      printf("StarterTaskHandler: %d\n", k);
      osDelay(1);
  }
  osThreadSuspend(starterTaskHandle);
  /* USER CODE END 5 */ 
}

/* USER CODE BEGIN Header_UartTaskHandler */
/**
* @brief Function implementing the uartTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_UartTaskHandler */
void UartTaskHandler(void const * argument)
{
  /* USER CODE BEGIN UartTaskHandler */
    printf("UartTaskHandler started!\n");
  /* Infinite loop */
    for(int k = 0; k < 128; k++) {
        printf("UartTaskHandler: %d\n", k);
        osDelay(1);
    }
    osThreadSuspend(uartTaskHandle);
  /* USER CODE END UartTaskHandler */
}

/* USER CODE BEGIN Header_ProcessTaskHandler */
/**
* @brief Function implementing the processTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_ProcessTaskHandler */
void ProcessTaskHandler(void const * argument)
{
  /* USER CODE BEGIN ProcessTaskHandler */
    printf("ProcessTaskHandler started!\n");
  /* Infinite loop */
    for(int k = 0; k < 128; k++) {
        printf("ProcessTaskHandler: %d\n", k);
        osDelay(1);
    }
    osThreadSuspend(processTaskHandle);
  /* USER CODE END ProcessTaskHandler */
}

/* USER CODE BEGIN Header_WatchDogHandler */
/**
* @brief Function implementing the watchDogTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_WatchDogHandler */
void WatchDogHandler(void const * argument)
{
  /* USER CODE BEGIN WatchDogHandler */
    printf("WatchDogHandler started!\n");
  /* Infinite loop */
    for(int k = 0; k < 128; k++) {
        printf("WatchDogHandler: %d\n", k);
        osDelay(1);
    }

    osDelay(100);

    while (1) {
        osDelay(1);
    }
  /* USER CODE END WatchDogHandler */
}

 /**
  * @brief  Period elapsed callback in non blocking mode
  * @note   This function is called  when TIM1 interrupt took place, inside
  * HAL_TIM_IRQHandler(). It makes a direct call to HAL_IncTick() to increment
  * a global variable "uwTick" used as application time base.
  * @param  htim : TIM handle
  * @retval None
  */
void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef *htim)
{
  /* USER CODE BEGIN Callback 0 */

  /* USER CODE END Callback 0 */
  if (htim->Instance == TIM1) {
    HAL_IncTick();
  }
  /* USER CODE BEGIN Callback 1 */

  /* USER CODE END Callback 1 */
}

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */

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

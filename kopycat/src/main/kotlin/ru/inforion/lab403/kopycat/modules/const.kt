/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.modules

// Most usable bus width (until ULong and UInt not used can only be 63 bit long)
const val PIN = 1L
const val BUS01 = 1L shl 1
const val BUS02 = 1L shl 2
const val BUS03 = 1L shl 3
const val BUS04 = 1L shl 4
const val BUS05 = 1L shl 5
const val BUS06 = 1L shl 6
const val BUS07 = 1L shl 7
const val BUS08 = 1L shl 8
const val BUS09 = 1L shl 9
const val BUS10 = 1L shl 10
const val BUS12 = 1L shl 12
const val BUS15 = 1L shl 15
const val BUS16 = 1L shl 16
const val BUS18 = 1L shl 18
const val BUS20 = 1L shl 20
const val BUS24 = 1L shl 24
const val BUS28 = 1L shl 28
const val BUS30 = 1L shl 30
const val BUS32 = 1L shl 32
const val BUS42 = 1L shl 42
const val BUS44 = 1L shl 44
const val BUS48 = 1L shl 48
const val BUS52 = 1L shl 52
const val BUS56 = 1L shl 56
const val BUS60 = 1L shl 60

const val NAND_STATUS = 0L
const val NAND_IO = 1L
const val NAND_CMD = 2L
const val NAND_ADDRESS = 3L
const val NAND_BUS_SIZE = 4L

const val SD_DATA = 0L
const val SD_ARGUMENT = 1L
const val SD_COMMAND = 2L
const val SD_STATUS = 3L
const val SD_CONTROL = 4L
const val SD_RESPONSE = 5L
const val SD_BUS_SIZE = 6L

const val SPI_BUS_SIZE = 4L

const val PCI_REQUEST_SPACE_SIZE = 0xFFFF_FFFF

const val PCI_NOTHING_CONNECTED = 0xFFFF_FFFF

const val PCI_BDF_ENA_BIT = 31
val PCI_BDF_BUS_RANGE = 23..16
val PCI_BDF_DEVICE_RANGE = 15..11
val PCI_BDF_FUNC_RANGE = 10..8
val PCI_BDF_REG_RANGE = 7..0

const val PCI_CSR0 = 0
const val PCI_CSR1 = 1
const val PCI_CSR2 = 2
const val PCI_CSR3 = 3
const val PCI_CSR4 = 4
const val PCI_CSR5 = 5
const val PCI_CSR6 = 6
const val PCI_CONF = 7
const val PCI_SPACES_COUNT = 8

const val PCI_INTERRUPTS_COUNT = 4

const val ATA_PARAM_AREA = 0
const val ATA_DATA_AREA = 1

const val ATA_CURRENT_HEADS_PRM_ID = 55
const val ATA_CURRENT_SECTORS_PRM_ID = 56

const val ATA_SECTOR_SIZE = 512

const val ATA_BUS_SIZE = BUS60

/**
 * {RU}Адрес регистра ведущей шины UART/TERMINAL для чтения/записи данных для взаимодействия по шине UART{RU}
 */
const val UART_MASTER_BUS_DATA = 0L

/**
 * {RU}Адрес регистра ведущей шины UART/TERMINAL для чтения/записи параметров для взаимодействия по шине UART{RU}
 */
const val UART_MASTER_BUS_PARAM = 1L

/**
 * {RU}Количество адресов ведущей шины UART/TERMINAL для взаимодействия по шине UART{RU}
 */
const val UART_MASTER_BUS_SIZE = 2

/**
 * {RU}
 * Параметр используется для соединения контроллера UART с терминалом.
 * Терминал должен возвращать 1, если оконечное устройство включено и успешно инициализированно.
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_MASTER_ENABLE = 0

/**
 * {RU}
 * Параметр используется для соединения контроллера UART с терминалом.
 * Терминал должен возвращать 1, если оконечно устройство поддерживает прием данных по UART
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_MASTER_RX_ENABLE = 1

/**
 * {RU}
 * Параметр используется для соединения контроллера UART с терминалом.
 * Терминал должен возвращать 1, если оконечно устройство поддерживает передачу данных по UART
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_MASTER_TX_ENABLE = 2

/**
 * {RU}
 * Параметр используется для соединения контроллера UART с терминалом.
 * Терминал должен возвращать 1, если в буфере приема ничего нет.
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_MASTER_RX_UNDERFLOW = 3

/**
 * {RU}
 * Параметр используется для соединения контроллера UART с терминалом.
 * Терминал должен возвращать 1, если в буфере передачи заполнен
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_MASTER_TX_OVERFLOW = 4

/**
 * {RU}Размер ведомой шины UART/TERMINAL для взаимодействия с терминалом{RU}
 */
const val UART_SLAVE_BUS_SIZE = 1

/**
 * {RU}Адрес на ведомой шине UART/TERMINAL для сигнализации о том, что в терминали присутсвуют новые данные{RU}
 */
const val UART_SLAVE_BUS_REQUEST = 0L

/**
 * {RU}
 * Параметр используется для соединения контроллера терминала с UART.
 * Терминал записывает 1 по указанному SS, если данные были отправлены (выведены).
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_SLAVE_DATA_RECEIVED = 0

/**
 * {RU}
 * Параметр используется для соединения контроллера терминала с UART.
 * Терминал записывает 1 по указанному SS, если данные были приняты в терминал.
 * Должен быть использован в ss методах read/write
 * {RU}
 */
const val UART_SLAVE_DATA_TRANSMITTED = 1
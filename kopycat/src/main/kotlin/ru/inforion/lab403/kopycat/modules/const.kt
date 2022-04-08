/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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

// Most usable bus width
const val PIN = 1uL

const val BUS00 = 0x0000_0000_0000_0001uL
const val BUS01 = 0x0000_0000_0000_0002uL
const val BUS02 = 0x0000_0000_0000_0004uL
const val BUS03 = 0x0000_0000_0000_0008uL

const val BUS04 = 0x0000_0000_0000_0010uL
const val BUS05 = 0x0000_0000_0000_0020uL
const val BUS06 = 0x0000_0000_0000_0040uL
const val BUS07 = 0x0000_0000_0000_0080uL

const val BUS08 = 0x0000_0000_0000_0100uL
const val BUS09 = 0x0000_0000_0000_0200uL
const val BUS10 = 0x0000_0000_0000_0400uL
const val BUS11 = 0x0000_0000_0000_0800uL

const val BUS12 = 0x0000_0000_0000_1000uL
const val BUS13 = 0x0000_0000_0000_2000uL
const val BUS14 = 0x0000_0000_0000_4000uL
const val BUS15 = 0x0000_0000_0000_8000uL

const val BUS16 = 0x0000_0000_0001_0000uL
const val BUS17 = 0x0000_0000_0002_0000uL
const val BUS18 = 0x0000_0000_0004_0000uL
const val BUS19 = 0x0000_0000_0008_0000uL

const val BUS20 = 0x0000_0000_0010_0000uL
const val BUS21 = 0x0000_0000_0020_0000uL
const val BUS22 = 0x0000_0000_0040_0000uL
const val BUS23 = 0x0000_0000_0080_0000uL

const val BUS24 = 0x0000_0000_0100_0000uL
const val BUS25 = 0x0000_0000_0200_0000uL
const val BUS26 = 0x0000_0000_0400_0000uL
const val BUS27 = 0x0000_0000_0800_0000uL

const val BUS28 = 0x0000_0000_1000_0000uL
const val BUS29 = 0x0000_0000_2000_0000uL
const val BUS30 = 0x0000_0000_4000_0000uL
const val BUS31 = 0x0000_0000_8000_0000uL

const val BUS32 = 0x0000_0001_0000_0000uL
const val BUS33 = 0x0000_0002_0000_0000uL
const val BUS34 = 0x0000_0004_0000_0000uL
const val BUS35 = 0x0000_0008_0000_0000uL

const val BUS36 = 0x0000_0010_0000_0000uL
const val BUS37 = 0x0000_0020_0000_0000uL
const val BUS38 = 0x0000_0040_0000_0000uL
const val BUS39 = 0x0000_0080_0000_0000uL

const val BUS40 = 0x0000_0100_0000_0000uL
const val BUS41 = 0x0000_0200_0000_0000uL
const val BUS42 = 0x0000_0400_0000_0000uL
const val BUS43 = 0x0000_0800_0000_0000uL

const val BUS44 = 0x0000_1000_0000_0000uL
const val BUS45 = 0x0000_2000_0000_0000uL
const val BUS46 = 0x0000_4000_0000_0000uL
const val BUS47 = 0x0000_8000_0000_0000uL

const val BUS48 = 0x0001_0000_0000_0000uL
const val BUS49 = 0x0002_0000_0000_0000uL
const val BUS50 = 0x0004_0000_0000_0000uL
const val BUS51 = 0x0008_0000_0000_0000uL

const val BUS52 = 0x0010_0000_0000_0000uL
const val BUS53 = 0x0020_0000_0000_0000uL
const val BUS54 = 0x0040_0000_0000_0000uL
const val BUS55 = 0x0080_0000_0000_0000uL

const val BUS56 = 0x0100_0000_0000_0000uL
const val BUS57 = 0x0200_0000_0000_0000uL
const val BUS58 = 0x0400_0000_0000_0000uL
const val BUS59 = 0x0800_0000_0000_0000uL

const val BUS60 = 0x1000_0000_0000_0000uL
const val BUS61 = 0x2000_0000_0000_0000uL
const val BUS62 = 0x4000_0000_0000_0000uL
const val BUS63 = 0x8000_0000_0000_0000uL
const val BUS64 = 0xFFFF_FFFF_FFFF_FFFFuL // Now we can't define full 64-bit bus, so the last address is unavailable (also in IDA)

const val NAND_STATUS = 0uL
const val NAND_IO = 1uL
const val NAND_CMD = 2uL
const val NAND_ADDRESS = 3uL
const val NAND_BUS_SIZE = 4uL

const val SD_DATA = 0uL
const val SD_ARGUMENT = 1uL
const val SD_COMMAND = 2uL
const val SD_STATUS = 3uL
const val SD_CONTROL = 4uL
const val SD_RESPONSE = 5uL
const val SD_BUS_SIZE = 6uL

const val SPI_BUS_SIZE = 4L

const val PCI_ECAM_BUS_SIZE = 0x1000_0000
const val PCI_ECAM_DEVICE_SIZE = 4096uL

const val PCI_REQUEST_SPACE_SIZE = 0xFFFF_FFFFuL

const val PCI_NOTHING_CONNECTED = 0xFFFF_FFFFuL

const val PCI_BDF_ENA_BIT = 31
val PCI_BDF_BUS_RANGE = 23..16
val PCI_BDF_DEVICE_RANGE = 15..11
val PCI_BDF_FUNC_RANGE = 10..8
val PCI_BDF_REG_RANGE = 7..0

const val PCI_MEM_AREA = 0
const val PCI_IO_AREA = 1
const val PCI_UNDEF_AREA = -1

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
const val UART_MASTER_BUS_DATA = 0uL

/**
 * {RU}Адрес регистра ведущей шины UART/TERMINAL для чтения/записи параметров для взаимодействия по шине UART{RU}
 */
const val UART_MASTER_BUS_PARAM = 1uL

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
const val UART_SLAVE_BUS_REQUEST = 0uL

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
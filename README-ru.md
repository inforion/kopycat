<img src="https://github.com/inforion/kopycat/assets/55213322/98846b70-cd79-4253-a7f0-29d8905b61e1" width="384">

**Kopycat** — это эмулятор систем с несколькими процессорными архитектурами и пользовательского уровня (с модулем VEOS).

# Описание

Основные особенности:

- Простота сборки нового устройства. Настройте свою платформу с помощью Kotlin.
- Лёгкая кастомизация. Создавайте собственные модули платформы на Kotlin.
- Кроссплатформенность. Kopycat использует JVM в качестве основы и может работать на Windows, Linux и OSX.
- Полное соответствие. Представление виртуальной платформы идентично блок-схеме эмулируемого устройства.
- Поддержка множества архитектур: MIPS, ARM, MSP430, v850ES, x86.
- Режим пользовательского уровня. Возможность эмуляции отдельного ELF-файла без полной эмуляции системы.

Проект включает:

**Ядра процессоров:** ARMv6, ARMv6M, ARMv7, MIPS, MSP430, v850ES, x86, PowerPC (E500v2)

**Микроконтроллеры (MCU):** Cortex-M0, STM32F0xx, MSP430x44x, PIC32MZ, P2020, Atom 2758, ElanSC520

---

# Предварительные требования
Для использования Kopycat убедитесь, что у вас установлены:
- **Java Development Kit (JDK) 11**
- **Socat**: Для взаимодействия с терминалом
- **Docker или Podman** (опционально)

___

# Руководство пользователя
Целью данного руководства является обучение запуску и использованию эмулятора *Kopycat* "с нуля".
Краткий обзор:
- Первым шагом является получение дистрибутива Linux, который будет грузиться в эмуляторе. В разделе описывается сборка дистрибутива при помощи утилиты Buildroot.
- Далее идет описание процесса развертывания эмулятора для того, чтобы можно было начать работать с ним.
- И наконец, после запуска эмулятора, описание того, как происходит работа с сетью (E1000) и диском (SATA). Раздел позволит расширить навыки использования *Kopycat*, в частности, работать с сетью и передаваемым с хоста диском.

>**_Примечание:_** В руководстве подразумевается, что вы уже клонировали проект *Kopycat* на ваше устройство. Если нет:`git clone https://github.com/inforion/kopycat.git`

---
## 1. Подготовка дистрибутива при помощи Buildroot

**Buildroot** — это инструмент, который упрощает и автоматизирует процесс сборки полноценной Linux-системы для встраиваемых устройств, используя механизм кросс-компиляции.

Настроить toolchain и конфигурацию ядра можно командами `make menuconfig` и `make linux-menuconfig`, предоставляющими графический интерфейс. Сборка происходит выполнением команды `make`

После конфигурирования и сборки, артефакты будут лежать в директории `.\output\images`

Подробнее про возможности Buildroot в официальной документации - [Buildroot - Making Embedded Linux Easy](https://buildroot.org/docs.html)

### Сборка ядра для kopycat x86 (Version 0.11.0+)
В директории `.\kopycat-modules\tops\demolinux\src\main\buildroot` находится ContainterFile для сборки образа с Buildroot со сконфигурированным ядром Linux для x86. Для сборки можно использовать Docker или Podman (команды аналогичные).
1. Переходим в нужную директорию:
   `cd .\kopycat-modules\tops\demolinux\src\main\buildroot`
2. Собираем образ:
   `docker build -f .\Containerfile -t my-buildroot .`
3. После того как образ был собран, вытаскиваем из него ядро и root.cpio:
```
docker create --name temp-container my-buildroot
docker cp temp-container:/build/buildroot/output/images ./images
docker rm temp-container
```
Образ ядра и файловой системы будут лежать в директории `.\kopycat-modules\tops\demolinux\src\main\buildroot\images`

Для сборки ядра под MIPS аналогичные шаги в директории `.\kopycat-modules\tops\demolinux_mips\src\main\buildroot`

### Сборка ядра для kopycat x86 (Version 0.10.0)
В директории `.\kopycat-modules\tops\demolinux\src\main\buildroot` находятся архив с buildroot (`buildroot-2023.11.1.tar.gz`) sh скрипты для конфигурирования и сборки.
1. Распаковываем архив с buildroot:
```
tar xf buildroot-2023.11.1.tar.gz
mv buildroot-2023.11.1 buildroot-unpacked
```
2. Запускаем скрипт для сборки
   `./Build.sh`
3. Образ ядра и файловой системы будут лежать в `.\kopycat-modules\tops\demolinux\src\main\buildroot\buildroot-unpacked/output/images/`
>*Примечание*: Для Windows можно использовать WSL или Docker в качестве виртуального окружения для запуска скриптов и сборки ядра

---
## 2. Развертывание эмулятора

### 2.1 Руководство для Linux

#### Шаги для запуска и проверки Demolinux с Kopycat:

1. **Проверьте версию Java**  

   Убедитесь, что используется Java 11:
    ```bash
    java --version
    ```

   Если установлено несколько версий JDK, установите переменные окружения `PATH` и `JAVA_HOME`, указывая на JDK 11.

2. **Соберите kotlin-extensions**

    ```bash
    git clone https://github.com/inforion/kotlin-extensions.git
    cd kotlin-extensions
    ./gradlew publishToMavenLocal
    ```

3. **Откройте проект и соберите Kopycat**

    ```bash
    ./gradlew createKopycatConfig
    ./gradlew buildKopycatModule
    ```

4. **Установите socat**

    ```bash
    sudo apt install socat
    ```

5. **Запустите эмулятор с помощью скрипта**

   Перед запуском эмулятора, убедитесь, что у вас есть все необходимые для работы ресурсы (ядро и rootfs для demolinux).
   Они должны находиться в директории `./kopycat-modules/**/src/main/resources/**/binaries` модуля или в `./kopycat/resources/**/binaries`. 
   Первая директория будет использоваться, чтобы положить ядро в jar во время сборки, а вторая, чтобы получить его во время выполнения программы.
   Запустите скрипт:
    ```bash
    ./kopycat-private/temp/config/bash/demolinux-default.sh
    ```
   >**_Примечание:_** Также, стоит учесть, что для demolinux_x86, например, Kopycat по-умолчанию ищет ядро и rootfs как "bzImage.gz" и "rootfs.cpio.gz". Если вы хотите переопределить названия ресурсов (например, чтобы использовать их без сжатия), вы можете добавить их в параметры топ-модуля в команде запуска программы `-p "...,bzImageName=bzImage,initRdName=rootfs.cpio"`
   
   >**_Примечание:_** Ядро, предоставляемое в процессе выполнения, имеет больший приоритет.

6. **Загрузите и запустите демо-снапшот с прогрузившемся дистрибутивом в Kopycat**  

   В консоли Kopycat выполните:
    ```bash
    kc.load("snapshot_name_from_kopycat-private/temp/demolinux/")
    kc.start()
    ```

7. **Подключитесь через socat**

    ```bash
    socat rawer,escape=0x0f tcp4:localhost:64130
    ```

8. **Проверьте работу Demolinux**

   Введите следующие команды и дождитесь вывода:
   ```bash
   ls -l
   cat /proc/meminfo
   ```

---

### 2.2 Руководство для Windows

#### Шаги для запуска и проверки верхнего модуля Demolinux с Kopycat:

1. **Проверьте версию Java**  

   Убедитесь, что используется Java 11:
    ```powershell
    java --version
    ```
   Если установлено несколько версий JDK, настройте `PATH` и `JAVA_HOME` на путь к JDK 11.

2. **Соберите kotlin-extensions**

   ```powershell
   git clone https://github.com/inforion/kotlin-extensions.git
   cd kotlin-extensions
   ./gradlew publishToMavenLocal
   ```
   
3. **Соберите Kopycat**

   ```powershell
   ./gradlew createKopycatConfig
   ./gradlew buildKopycatModule
   ```

4. **Установите socat**

   Есть разные способы установить/собрать socat под Windows.
   Например, вы можете установить его при помощи терминала msys:
   ```
   pacman -S socat
   ```
   Опционально, добавьте путь к socat в Path, чтобы можно было вызывать его из Powershell

5. **Запустите PowerShell-скрипт**

   ```powershell
   .\kopycat-private\temp\config\powershell\demolinux-default.ps1
   ```

6. **Загрузите и запустите демо-снапшот с прогрузившемся дистрибутивом в Kopycat**  

   В консоли Kopycat выполните:
   ```powershell
   kc.load("snapshot_name_from_kopycat-private\temp\demolinux\")
   kc.start()
   ```

7. **Подключитесь с помощью socat**

   Выполните:
   ```Powershell
   socat rawer,escape=0x0f tcp4:localhost:64130
   ```

8. **Проверьте работу Demolinux**

   Введите следующие команды и дождитесь вывода:
   ```bash
   ls -l
   cat /proc/meminfo
   ```

---

### 2.3 Руководство по запуску Kopycat в контейнере Docker

Вы можете запустить Kopycat используя Dockerfile в репозитории проекта

1. **Убедитесь, что у вас установлен Docker**

    ```bash
    docker --version
    ```
2. **Соберите Docker-образ**

   Dockerfile находится в директории проекта.
    ```bash
    docker build -t kopycat .
    ```
3. **Запустите Docker-контейнер**

   Чтобы запустить контейнер, выполните команду:
   ```bash
   docker run -it --name kopycat-container kopycat /bin/bash
   ```
   Рабочая директория контейнера будет иметь следующее содержание:
   ```
   opt/kopycat
   ├── demolinux-default-net.sh
   ├── demolinux-default-x32.sh
   ├── demolinux-default.sh
   ├── kopycat-modules // runtime scripts
   └── production // JARs
   ```
   Если вы хотите использовать свое ядро или снапшоты, вы можете использовать volume:
   ```bash
   docker run \
   -v ./temp:/opt/kopycat/temp/demolinux \
   -v ./pathToTheResourceDir:/opt/kopycat/resources/ru/inforion/lab403/kopycat/modules/demolinux/binaries \
   -it --name kopycat-container kopycat /bin/bash
   ```
   Первый volume содержит снапшоты, а второй это директория с ядром, rootfs и так далее.
   Вы также можете использовать `docker cp` вместо volume.

4. **Запустите Kopycat внутри контейнера**

    ```bash
    ./demolinux-default.sh
    ```
5. **Загрузите демо-снапшот и запустите эмулятор**

   Выполните в консоли:
    ```
    kc.load("snapshot_name_from_kopycat\temp\demolinux\")
    kc.start()
    ```
6. **Подключитесь при помощи Socat в Docker-контейнере**

   ```bash
   docker exec -it kopycat-container bash
   socat -,rawer,escape=0x0f tcp:localhost:64130
   ```
7. **Проверьте работу Demolinux**

   Введите следующие команды и дождитесь вывода:
   ```bash
   ls -l
   cat /proc/meminfo
   ```
   
---

## 3. Проверка работы сети (E1000) и диска (SATA) в эмуляторе (demolinux x86)

### 3.1 Диск

Создаем диск в корне проекта `fallocate -l 30M disks/demo.bin`  
Можно проверить, что эмулятор видит диск как устройство:

```
# fdisk -l
Disk /dev/sda: 0 MB, 65536 bytes, 128 sectors
0 cylinders, 255 heads, 63 sectors/track
Units: sectors of 1 * 512 = 512 bytes

Disk /dev/sda doesn't contain a valid partition table
Disk /dev/sdb: 30 MB, 31457280 bytes, 61440 sectors // Наш диск
3 cylinders, 255 heads, 63 sectors/track
Units: sectors of 1 * 512 = 512 bytes

Disk /dev/sdb doesn't contain a valid partition table
```

Далее, требуется создать на диске таблицу разделов и один раздел ext4. Удобнее делать это на хосте (или в WSL для Windows):

```
// привязываем к устройству
# sudo losetup -fP --show ./demo.bin
//← вернётся, например, /dev/loop0
// создаем таблицу разделов
# sudo fdisk /dev/loop0

// команды fdisk
o    очистить старую таблицу и создать DOS
n    новый раздел
p    primary
1    номер 1
     первый сектор – <Enter>
     последний сектор – <Enter> (весь диск)
w    записать и выйти

// Форматируем раздел
# sudo mkfs.ext4 /dev/loop0p1 -L GUESTDISK
mke2fs 1.46.5 (30-Dec-2021)
Creating filesystem with 7672 4k blocks and 7680 inodes

Allocating group tables: done
Writing inode tables: done
Creating journal (1024 blocks): done
Writing superblocks and filesystem accounting information: done

// Отвязываем устройство
# sudo losetup -d /dev/loop0

```

Теперь, когда на диске есть отформатированный раздел, можно попытаться смонтировать его в эмуляторе и проверить работу  
В эмуляторе:

```
# partprobe /dev/sdb
 sdb: sdb1
# mkdir data
# mount /dev/sdb1 data
EXT4-fs (sdb1): mounted filesystem with ordered data mode. Opts: (null)
# cd data
# echo test test test > testfile
# cd ..
# umount data && sync
```

В диск был записан файл testfile с содержимым "test test test".  
Можем убедиться в наличии файла и его содержимого вновь на хосте (или WSL):

```
# sudo losetup --find --show --partscan ./demo.bin
/dev/loop0
# sudo mkdir /mnt/testdisk
# sudo mount /dev/loop0p1 /mnt/testdisk
# ls /mnt/testdisk
lost+found  testfile
# cat /mnt/testdisk/testfile
test test test
```

### 3.2 Сеть

Для проверки работы сети требуется поднять виртуальный TAP-интерфейс на хосте на порту, указанном в параметрах запуска эмулятора:

```bash
sudo socat tun:192.168.19.2/24,tun-type=tap,iff-up,iff-no-pi tcp-listen:30003
```

На Windows аналогичное действие можно провернуть при помощи WSL и проброса портов.  
В Windows (Powershell):

```Powershell
PS wsl hostname -I
172.27.181.15 172.17.0.1 10.69.69.1
PS netsh interface portproxy add v4tov4 `
>>   listenaddress=0.0.0.0 listenport=30003 `
>>   connectaddress=172.27.181.15 connectport=30003

PS New-NetFirewallRule -DisplayName "WSL PortProxy 30003" `
>>   -Direction Inbound -Protocol TCP -LocalPort 30003 -Action Allow
```

В WSL:

```bash
sudo socat tun:192.168.19.2/24,tun-type=tap,iff-up,iff-no-pi tcp-listen:30003
```

>**_Примечание:_** Все перечисленные ранее шаги нужно выполнить **ДО ЗАПУСКА ЭМУЛЯТОРА!**  

Для теста поднимем также python http.server в WSL в директории с каким-нибудь тестовым файлом:  
`python3 -m http.server`  
Наконец, проверка работы сети в эмуляторе:

```
# ip link set eth0 up
IPv6: ADDRCONF(NETDEV_UP): eth0: link is not ready
e1000e: eth0 NIC Link is Up 1000 Mbps Full Duplex, Flow Control: Rx/Tx
IPv6: ADDRCONF(NETDEV_CHANGE): eth0: link becomes ready
# ip addr add 192.168.19.10/24 dev eth0
# wget http://192.168.19.2:8000/test
Connecting to 192.168.19.2:8000 (192.168.19.2:8000)
saving to 'test'
test                 100% |********************************|    10  0:00:00 ETA
'test' saved
# cat test
test file
```

### 3.3 Global network
Если вы хотите подключиться к глобальной сети, потребуется выполнить несколько дополнительных шагов.

Мы настроим NAT между интерфейсом TAP, созданным в предыдущей части для связи эмулятора и хоста, 
и интерфейсом, который используется для подключения к глобальной сети на хосте (WSL для Windows).

Сначала необходимо включить IP Forwarding. Добавьте следующую строку в файл `/etc/sysctl.conf`:
```Bash
sudo sysctl -w net.ipv4.ip_forward=1
```
Далее нужно настроить NAT с помощью iptables:
```
sudo iptables -t nat -A POSTROUTING -s 192.168.19.0/24 -o eth0 -j MASQUERADE
sudo iptables -A FORWARD -i tap0 -o eth0 -j ACCEPT
sudo iptables -A FORWARD -i eth0 -o tap0 -m state --state RELATED,ESTABLISHED -j ACCEPT
```
*eth0* здесь это интерфейс для доступа к глобальной сети. 
Вы можете определить свой интерфейс с помощью команды `ip addr show`

Теперь нужно настроить маршрут по умолчанию в эмуляторе:
```
# ip route show
192.168.19.0/24 dev eth0 scope link  src 192.168.19.10
# ip route add default via 192.168.19.2 dev eth0
# ip route show
default via 192.168.19.2 dev eth0
192.168.19.0/24 dev eth0 scope link  src 192.168.19.10
```
192.168.19.10 - адрес хоста в созданной в предыдущем пункте сети.
Наконец, попробуем связаться с 8.8.8.8 (Google public DNC):
```
# ping 8.8.8.8
PING 8.8.8.8 (8.8.8.8): 56 data bytes
64 bytes from 8.8.8.8: seq=0 ttl=100 time=4.000 ms
64 bytes from 8.8.8.8: seq=1 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=2 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=3 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=4 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=5 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=6 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=7 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=8 ttl=100 time=0.000 ms
64 bytes from 8.8.8.8: seq=9 ttl=100 time=0.000 ms
^C
--- 8.8.8.8 ping statistics ---
10 packets transmitted, 10 packets received, 0% packet loss
round-trip min/avg/max = 0.000/0.400/4.000 ms
```
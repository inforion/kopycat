rem "C:\mgc\embedded\codebench\bin\mips-linux-gnu-gcc.exe" -EL memo.c -o memo.o
rem "C:\mgc\embedded\codebench\bin\mips-linux-gnu-ld.exe" -Ttext-segment 0x80400000 -o memo.elf -EL memo.o 

mips-linux-gnu-gcc.exe -EL -fno-plt memo.c -o memo.elf
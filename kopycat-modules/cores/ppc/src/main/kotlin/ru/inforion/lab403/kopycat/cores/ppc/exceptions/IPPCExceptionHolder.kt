package ru.inforion.lab403.kopycat.cores.ppc.exceptions


 
interface IPPCExceptionHolder {
    fun accessDataException(where: Long, write: Boolean): PPCHardwareException
    fun accessInstructionException(where: Long): PPCHardwareException
    fun tlbDataException(where: Long, write: Boolean, ea: Long, AS: Long): PPCHardwareException
    fun tlbInstructionException(where: Long, ea: Long, AS: Long): PPCHardwareException
}
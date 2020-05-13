package ru.inforion.lab403.kopycat.cores.ppc.exceptions



object PPCExceptionHolder_Embedded : IPPCExceptionHolder {

    override fun accessDataException(where: Long, write: Boolean): PPCHardwareException {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun accessInstructionException(where: Long): PPCHardwareException {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tlbDataException(where: Long, write: Boolean, ea: Long, AS: Long): PPCHardwareException {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tlbInstructionException(where: Long, ea: Long, AS: Long): PPCHardwareException {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
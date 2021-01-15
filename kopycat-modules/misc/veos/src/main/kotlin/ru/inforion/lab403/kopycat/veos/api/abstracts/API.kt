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
package ru.inforion.lab403.kopycat.veos.api.abstracts

import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.interfaces.IOnlyAlias
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API.SerializableMethod.Companion.toSerializableMethod
import ru.inforion.lab403.kopycat.veos.api.annotations.APIFunc
import ru.inforion.lab403.kopycat.veos.api.cherubim.ArgumentList
import ru.inforion.lab403.kopycat.veos.api.cherubim.Cherubim
import ru.inforion.lab403.kopycat.veos.api.datatypes.LongLong
import ru.inforion.lab403.kopycat.veos.api.datatypes.Sizet
import ru.inforion.lab403.kopycat.veos.api.datatypes.VaArgs
import ru.inforion.lab403.kopycat.veos.api.datatypes.VaList
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.pointers.*
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction


abstract class API(val os: VEOS<*>) : IAutoSerializable, IConstructorSerializable {

    companion object {
        @Transient val log = logger(FINE)
    }

    private inline fun <reified T> availableCallbacks() = javaClass.declaredFields
            .associate {
                it.isAccessible = true
                it.name to it.get(this)
            }
            .filterValues { it is T }
            .map { it.key to it.value as T }
            .toMap()

    class APIType<T : Any>(val cls: Class<out T>, val type: ArgType, val cast: (index: Int, value: Long) -> T) : Serializable

    class APIRetType<T : Any>(val cls: Class<out T>, val toResult: (T) -> APIResult) : Serializable

    protected inline fun <reified T : Any> type(type: ArgType, noinline cast: (index: Int, value: Long) -> T) =
            APIType(T::class.java, type, cast).also { apiTypes.add(it) }

    protected inline fun <reified T : Any> ret(noinline toResult: (T) -> APIResult) =
            APIRetType(T::class.java, toResult).also { apiRetTypes.add(it) }

    @DontAutoSerialize
    protected val apiTypes = mutableListOf<APIType<out Any>>()

    @DontAutoSerialize
    protected val apiRetTypes = mutableListOf<APIRetType<out Any>>()

    class SerializableMethod(private var method: Method) : Serializable {
        companion object {
            fun KFunction<*>.toSerializableMethod() = SerializableMethod(javaMethod!!)
        }

        private fun writeObject(output: ObjectOutputStream) = with(output) {
            writeObject(method.declaringClass)
            writeUTF(method.name)
            writeObject(method.parameterTypes)
        }

        private fun readObject(input: ObjectInputStream) {
            val declaringClass = input.readObject() as Class<*>
            val methodName = input.readUTF()
            val parameterTypes = input.readObject() as Array<Class<*>>
            method = try {
                declaringClass.getMethod(methodName, *parameterTypes)
            } catch (e: java.lang.Exception) {
                throw IOException("Error occurred resolving deserialized method '${declaringClass.simpleName}.${methodName}'")
            }
        }

        fun invoke(obj: Any?, vararg args: Any?) = method.kotlinFunction!!.call(obj, *args)

        val name get() = method.name.substringAfterLast(".")
    }

    class LegacyAbstractionLayer constructor(
            val api: API,
            val function: SerializableMethod,
            val types: Array<APIType<Any>>,
            val resultType: APIRetType<Any>?
    ) : APIFunction(function.name), IOnlyAlias {

        override val args = types.map { it.type }.toTypedArray()

        override fun exec(name: String, vararg argv: Long): APIResult {
            val arguments = (argv zip types).mapIndexed { i, (value, type) -> type.cast(i, value) }.toTypedArray()

            val result = try {
                function.invoke(api, *arguments)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }

            if (resultType == null)
                return APIResult.Void()

            return resultType.toResult(result!!)
        }
    }

    private fun toAPIFunction(function: KFunction<*>): LegacyAbstractionLayer {
        val types = function.parameters.drop(1).map { param ->
            apiTypes.find {
                val klass = param.type.classifier as KClass<*>
                it.cls == klass.javaObjectType
            }.sure { "Can't find type '${param.type.classifier}'" }
        }.toTypedArray()

        val resultType = when (function.returnType.classifier) {
            Unit::class -> null
            else -> apiRetTypes.find {
                val klass = function.returnType.classifier as KClass<*>
                it.cls == klass.javaObjectType
            }.sure { "Can't find return type '${function.returnType.classifier}'" }
        }

        return LegacyAbstractionLayer(this, function.toSerializableMethod(), types.cast(), resultType?.cast())
    }

    @DontAutoSerialize
    val ngFunctions by lazy {
        this::class.memberFunctions.filter {
            it.findAnnotation<APIFunc>() != null
        }.associate {
            it.name to toAPIFunction(it)
        }
    }

    @DontAutoSerialize
    val functions by lazy { availableCallbacks<APIFunction>() + ngFunctions }

    class nullsub(name: String, address: Long? = null) : APIFunction(name, address) {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.finer { "<$name> is dummyFunc" }
            return retval(0)
        }
    }

    /**
     * If API suppose to have program input arguments and
     * have something especial to do with that then
     * main function putMainArgs should be override
     */
    open fun init(argc: Long, argv: Long, envp: Long) = Unit

    open fun setErrno(error: Exception?): Unit = throw NotImplementedError("setErrno() is not implemented!")

    // TODO: full use of system instead of os
    protected inline val sys get() = os.sys

    protected inline val ra get() = os.abi.returnAddressValue

    /**
     * This function only for blocking operation (non-async)
     */
    protected inline fun <T> nothrow(default: T, block: () -> T) = try {
        block()
    } catch (error: Exception) {
        log.finest { "[0x${ra.hex8}] C errno layer got exception -> $error" }
        setErrno(error)
        default
    }

    private val cherubims = mutableMapOf<ArgumentList, Cherubim>()

    protected fun withCallback(vararg args: Long, block: (Cherubim) -> Any?): APIResult {
        val key = ArgumentList(*args, os.abi.returnAddressValue)
        val localCherub = cherubims[key]
        if (localCherub == null) {
            val newCherubim = Cherubim(os, *args)
            cherubims[key] = newCherubim
            thread {
                val result = when (val returned = block(newCherubim)) { // TODO: use new API lambdas
                    is Unit -> APIResult.Void()
                    is Int -> APIResult.Value(returned.asULong)
                    is Long -> APIResult.Value(returned)
                    else -> TODO("Not implemented result type")
                }
                cherubims.remove(key)
                newCherubim.fromInterrupted.add(result)
            }
            return newCherubim.fromInterrupted.take()
        } else {
            localCherub.toInterrupted.add(os.abi.readInt(os.sys.restoratorResult))
            return localCherub.fromInterrupted.take()
        }
    }


    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        // We don't want to really serialize these objects
        ngFunctions.forEach { (k, v) ->
            ctxt.withPrefix(k) {
                ctxt.addSerializedObject(v)
            }
        }
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        // We don't want to really deserialize these objects
        ngFunctions.forEach { (k, v) ->
            ctxt.withPrefix(k) {
                ctxt.addDeserializedObject(v)
            }
        }
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }

    /**
     * {EN}
     * Function throws [NullPointerException] exception hat is in fact segmentation fault if [condition] is met
     *
     * You can call this function whenever want to check if something goes wrong and program should be stopped
     *
     * @param condition is a condition to stop program (if true)
     * @param message is a message provider function
     * {EN}
     */
    protected inline fun segfault(condition: Boolean, address: Long = -1, message: () -> String) {
        if (condition) throw MemoryAccessError(ra, address, AccessAction.LOAD, message())
    }

    /**
     * {EN}
     * Function throws [NullPointerException] exception that is in fact segmentation fault if provided pointer is null
     *
     * Calling of this function is not necessary just make possible to show a specific message if crash occurred
     *
     * @param pointer is a pointer to check on null
     * @param message is a message provider function
     * {EN}
     */
    protected inline fun <T> segfault(pointer: Pointer<T>, message: () -> String) =
            segfault(pointer.isNull, pointer.address, message)

    init {
        type(ArgType.Char) { _, it -> it.asChar }
        type(ArgType.Short) { _, it ->it.asShort }
        type(ArgType.Int) { _, it ->it.asInt }
        type(ArgType.Long) { _, it -> it }
        type(ArgType.LongLong) { _, it -> LongLong(it like os.abi.types.longLong) }

        type(ArgType.Char) { _, it -> it.asByte }
        type(ArgType.Short) { _, it -> it.ushort }
        type(ArgType.Int) { _, it -> it.uint }
        type(ArgType.LongLong) { _, it -> it.ulong }

        val sizetArgType = when (os.abi.sizetDatatype) {
            Datatype.QWORD -> ArgType.LongLong
            Datatype.DWORD -> ArgType.Long
            else -> TODO("Not implemented for ${os.abi.sizetDatatype}")
        }

        type(sizetArgType) { _, it -> Sizet((it like os.abi.sizetDatatype).ulong) }

        type(ArgType.Pointer) { _, it -> BytePointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> CharPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> ShortPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> IntPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> LongPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> FunctionPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> VoidPointer(os.sys, it) }
        type(ArgType.Pointer) { _, it -> PointerPointer(os.sys, it) }
        type(ArgType.Pointer /* TODO: may cause bug if stack is empty */) { i, it -> VaArgs(os.sys, i) }
        type(ArgType.Pointer) { _, it -> VaList(os.sys, it) }

        ret<Char> { APIResult.Value(it.asLong) }
        ret<Short> { APIResult.Value(it.asLong) }
        ret<Int> { APIResult.Value(it.asLong) }
        ret<Long> { APIResult.Value(it, ArgType.Long) }
        ret<LongLong>  { APIResult.Value(it.toLong(), ArgType.LongLong) }

        ret<Sizet> { APIResult.Value(it.toLong(), sizetArgType) }

        ret<Byte> { APIResult.Value(it.asLong) }
        ret<UShort> { APIResult.Value(it.long) }
        ret<UInt> { APIResult.Value(it.long) }
        ret<ULong> { APIResult.Value(it.long) }

        ret<BytePointer> { APIResult.Value(it.address) }
        ret<CharPointer> { APIResult.Value(it.address) }
        ret<ShortPointer> { APIResult.Value(it.address) }
        ret<IntPointer> { APIResult.Value(it.address) }
        ret<FunctionPointer> { APIResult.Value(it.address) }
        ret<VoidPointer> { APIResult.Value(it.address) }
        ret<PointerPointer> { APIResult.Value(it.address) }

        ret<APIResult> { it }
    }
}
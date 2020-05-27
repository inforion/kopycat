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
package ru.inforion.lab403.kopycat.cores.base.common

import gnu.trove.map.hash.THashMap
import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.serializer.loadValue
import java.util.logging.Level.INFO


/**
 * {RU}
 * Компонент - базовый блок для построения системы взаимодействующих элементов.
 * Позволяет выстраивать иерархии компонентов. Не привязан к какой-то конкретной сущности.
 *
 * Внимание: будте осторожны при использовании наследования классов!
 * В результате вызова конструктора [Component] происходит добавление данного экземпляра в иерархию компонентов.
 * Таким образом, при переопределении свойств (тип которых унаследован от [Component]) в классе-наследнике,
 * убедитесь, что не происходит вызов конструктора в классе-родителе.
 * В противном случае, иерархия компонентов будет построенна некорректно ввиду присутствия в ней ненужной копии от
 * класса-родителя.
 *
 *
 * @param parent Родительский компонент (необязательный параметр)
 * @property name Произвольное имя объекта Компонента
 * @param plugin Имя плагина (необязательный параметр)
 *
 * @property components Набор-отображение (HashMap) дочерних компонентов (компонентов, входящих в данный компонент)
 * @property instanceName Имя текущего объекта
 * @property loadedPlugin Имя загруженного плагина
 * @property snapshotPlugin Имя плагина снимка состояния системы
 * @property snapshotLibrary Имя библиотеки снимка состояния системы
 * @property isTopInstance Флаг "Объект верхнего уровня" (отсутствует родитель)
 * @property parent Родительский компонент, в который включается текущий компонент
 * @property root Корневой компонент (по умолчанию, текущий объект - this)
 * {RU}
 *
 * {EN}
 * Component class. Component is basic block for build system of interacting elements.
 * It allows you to create a hierarchy of components. It doesn't tied to any particular entity.
 *
 * @param parent parent component (can be null if it doesn't have parent)
 * @property name component name
 * @param plugin plugin name (can be null)
 *
 * @property components hashmap of child components (components included in current component)
 * @property instanceName name of current component
 * @property loadedPlugin name of loaded plugin
 * @property snapshotPlugin name of plugin for snapshot
 * @property snapshotLibrary library name for snapshot
 * @property isTopInstance flag "this component if top component" - has no parent
 * @property parent parent component
 * @property root top component of current device (this by default)
 * {EN}
 */
open class Component(
        parent: Component? = null,
        final override val name: String,
        plugin: String? = null
): Iterable<Component>, ICoreUnit {

    companion object {
        val log = logger(INFO)
    }

    /**
     * {EN}Name of command for interactive emulator console.{EN}
     *
     * {RU}
     * Имя команды для текущего класса в интерактивной консоли эмулятора.
     * Для использования команд в консоли эмулятора.
     * @return строковое имя команды
     * {RU}
     **/
    override fun command(): String = name

    /**
     * {EN}
     * Object string representation
     *
     * @return string representation
     * {EN}
     *
     * {RU}
     * Строковое представление объекта
     * @return строка-представление объекта
     * {RU}
     **/
    override fun toString(): String = fullname()

    protected val components = THashMap<String, Component>()

    val instanceName: String = name
    val loadedPlugin: String get() = javaClass.simpleName
    var snapshotPlugin: String = plugin ?: loadedPlugin
        private set
    var snapshotLibrary: String = "veos"
        private set

    val isTopInstance: Boolean get() = parent == null

    var parent: Component? = parent
        private set

    @Suppress("LeakingThis")
    var root: Component = this
        private set

    /**
     * {EN}Execute lambda function [block] for all component of the hierarchy{EN}
     *
     * {RU}Выполнение функции обработчика [block] для всех компонентов заданного класса в иерархии (за исключением себя!){RU}
     **/
    inline fun <reified T: Component>forEachNested(block: (component: T) -> Unit) =
            getComponentsByClass<T>().forEach { block(it) }

    /**
     * {EN}Execute lambda function [block] for all component of the hierarchy{EN}
     *
     * {RU}Выполнение функции обработчика [block] для всех компонентов системы заданного класса{RU}
     **/
    inline fun <reified T: Component>forMeAndEachNested(block: (component: T) -> Unit) {
        block(this as T)
        getComponentsByClass<T>().forEach { block(it) }
    }

    /**
     * {RU}
     * Create components generator, in hierarchy until root component
     *
     * @return Последовательность компонентов до компонента root (this->this.parent->....->root)
     * {RU}
     *
     * {EN}
     * Create components generator, in hierarchy until root component
     *
     * @return sequence until root component (this->this.parent->....->root)
     * {EN}
     */
    fun seqComponentsUntilRoot() = generateSequence(this) { it.parent }

    /**
     * {RU}
     * Получение списка имён всех компонентов
     *
     * @return список имён дочерних компонентов
     * {RU}
     *
     * {EN}
     * Get all components name
     *
     * @return list of components name
     * {EN}
     */
    fun getComponentsNames(): List<String> = map { it.name }

    /**
     * {RU}
     * Получение списка дочерних компонентов, для которых успешно выполняется заданная функция (предикат)
     *
     * @param result Результирующий список компонентов
     * @param predicate Функция-фильтр для отбора компонентов
     *
     * @return Список дочерних компонентов
     * {RU}
     *
     * {EN}
     * Get list of children components for which the predicate was successful.
     *
     * @param result result component list
     * @param predicate filter-function for component selection
     *
     * @return List of children components
     * {EN}
     */
    fun getChildrenComponentsByPredicateTo(
            result: MutableCollection<in Component>,
            predicate: (Component) -> Boolean
    ): MutableCollection<in Component> = filterTo(result) {
        it.getChildrenComponentsByPredicateTo(result, predicate)
        predicate(it)
    }

    /**
     * {RU}
     * Получение всех компонентов, входящих в root и удовлетворяющих предикату
     *
     * @param result Результирующий список компонентов
     * @param predicate Функция-фильтр для отбора компонентов
     *
     * @return Список дочерних компонентов
     * {RU}
     *
     * {EN}
     * Get list of all children components for root for which the predicate was successful.
     *
     * @param result result component list
     * @param predicate filter-function for component selection
     *
     * @return List of children components
     * {EN}
     */
    fun getComponentsByPredicateTo(
            result: MutableCollection<in Component>,
            predicate: (Component) -> Boolean
    ): MutableCollection<in Component> = root.getChildrenComponentsByPredicateTo(result, predicate)

    /**
     * {RU}
     * Получение всех компонентов, удовлетворяющих предикату
     *
     * @param predicate Функция-фильтр для отбора компонентов
     *
     * @return Список дочерних компонентов
     * {RU}
     *
     * {EN}
     * Get list of all components for root for which the predicate was successful.
     *
     * @param predicate filter-function for component selection
     *
     * @return List of children components
     * {EN}
     */
    fun getComponentsByPredicate(predicate: (Component) -> Boolean) = getComponentsByPredicateTo(mutableListOf(), predicate)

    /**
     * {RU}
     * Получение компонентов по названию плагина
     *
     * @param name Имя плагина для поиска компонентов
     *
     * @return Список компонентов
     * {RU}
     *
     * {EN}
     * Get components by plugin name
     *
     * @param name Plugin name to search
     *
     * @return Components list
     * {EN}
     */
    fun getComponentsByPluginName(name: String) = getComponentsByPredicate { it.loadedPlugin == name }

    /**
     * {RU}
     * Получение компонентов по имени
     *
     * @param name Имя для поиска компонентов
     *
     * @return Список компонентов
     * {RU}
     *
     * {EN}
     * Get components by instance name
     *
     * @param name Name to search
     *
     * @return Components list
     * {EN}
     */
    fun getComponentsByInstanceName(name: String) = getComponentsByPredicate { it.instanceName == name }

    /**
     * {RU}
     * Поиск компонента по имени
     *
     * @param name Имя компонента для поиска
     *
     * @return Компонент или null
     * {RU}
     *
     * {EN}
     * Search component by name
     *
     * @param name component name to search
     *
     * @return component ot null
     * {EN}
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified R: Component>findComponentByInstanceName(name: String) =
            getComponentsByInstanceName(name).firstOrNull() as R?

    /**
     * {RU}
     * Получение компонента по имени
     *
     * @param name Имя компонента для поиска
     *
     * @return Компонент
     * {RU}
     *
     * {EN}
     * Get first of component by name
     *
     * @param name component name to search
     *
     * @return component
     * {EN}
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified R: Component>firstComponentByInstanceName(name: String) = findComponentByInstanceName<R>(name)!!

    /**
     * {RU}
     * Получение компонентов, соответствующих классу текущего компонента
     *
     * @return components
     * {RU}
     *
     * {EN}
     * All child components by class of current component
     *
     * @return components
     * {EN}
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified R: Component>getComponentsByClass() = getComponentsByPredicate { it is R } as MutableCollection<R>

    /**
     * {RU}
     * Получение всех компонентов, входящих в систему
     *
     * @return Список всех компонентов
     * {RU}
     *
     * {EN}
     * Get list of all components in the system.
     *
     * @return List of all components in the system
     * {EN}
     */
    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline fun getAllComponents() = getComponentsByPredicate { true } as MutableCollection<Component>

    /**
     * {RU}
     * Поиск компонента по классу текущего компонента
     *
     * @return Компоненты или null
     * {RU}
     *
     * {EN}
     * All child components by selected class
     *
     * @return components or null
     * {EN}
     */
    inline fun <reified R: Component>findComponentByClass() = getComponentsByClass<R>().firstOrNull()

    /**
     * {RU}
     * Первый найденный компонент по классу текущего компонента
     *
     * @return Компонент
     * {RU}
     *
     * {EN}
     * First founded component by selected class
     *
     * @return component
     * {EN}
     */
    inline fun <reified R: Component>firstComponentByClass() = findComponentByClass<R>()!!

    /**
     * {RU}
     * Итератор по набору дочерних компонентов
     *
     * @return Компонент
     * {RU}
     *
     * {EN}
     * Iterator by child components
     *
     * @return component
     * {EN}
     */
    final override fun iterator(): Iterator<Component> = components.values.iterator()

    /**
     * {RU}
     * Перегрузка оператора [] для удобного доступа к дочерним компонентам
     *
     * @param name Имя компонента
     *
     * @return Компонент
     * {RU}
     *
     * {EN}
     * Override operator [] for usable access to child components
     *
     * @param name component name
     *
     * @return component
     * {EN}
     */
    operator fun get(name: String): Component? = components[name]

    /**
     * {EN}Get component nesting level{EN}
     *
     * {RU}Получить уровень вложенности компонента в иерархии{RU}
     */
    fun getNestingLevel(): Int = seqComponentsUntilRoot().count()

    /**
     * {EN}Get component full name (considering other components of the hierarchy){EN}
     *
     * {RU}Получить полное имя компонента с учетом других компонентов иерархии{RU}
     */
    fun fullname(): String {
        if (parent == null)
            return instanceName
        return "${parent!!.fullname()}.$instanceName"
    }

    /**
     * {RU}
     * Add children component
     *
     * @param component дочерний компонент
     * {RU}
     *
     * {EN}
     * Add children component
     *
     * @param component children component
     * {EN}
     */
    fun add(component: Component) {
        if (component.name in components.keys)
            throw IllegalArgumentException("Components has identical name: ${component.name}")
        component.root = root
        component.parent = this
        components[component.name] = component
    }

    /**
     * {RU}
     * Инициализация компонента (и всех дочерних компонентов)
     *
     * @return success (true/false)
     * {RU}
     *
     * {EN}
     * Component initialization (and all children components)
     *
     * @return success (true/false)
     * {EN}
     */
    protected open fun initialize(): Boolean = all { it.initialize() }

    /**
     * {RU}Сброс компонента (и всех дочерних компонентов){RU}
     *
     * {EN}Reset components state (and all children components){EN}
     */
    override fun reset() {
        if (this == root) forEachNested<Component> { it.reset() }
    }

    /**
     * {RU}Прерывание работы компонента (и всех дочерних компонентов){RU}
     *
     * {EN}Terminate component execution (and all children components){EN}
     */
    override fun terminate() {
        if (this == root) forEachNested<Component> { it.terminate() }
    }

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt Контекст объекта-сериализатора
     *
     * @return Отображение сохраняемых свойств объекта
     * {RU}
     *
     * {EN}
     * Save object state to snapshot (Serialize)
     *
     * @param ctxt Serializer context
     *
     * @return map of object properties
     * {EN}
     */
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return ctxt.storeValues(
                "name" to name,
                "loadedPlugin" to loadedPlugin,
                "snapshotPlugin" to snapshotPlugin,
                "components" to components.map{ it.key to it.value.serialize(ctxt) }.toMap() )
    }

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt Контекст объекта-сериализатора
     * @param snapshot Отображение восстанавливаемых свойств объекта
     * {RU}
     *
     * {EN}
     * Restore object state to snapshot state (Deserialize)
     *
     * @param ctxt Serializer context
     * @param snapshot map of object properties
     * {EN}
     */
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val shName: String = loadValue(snapshot, "name")
        val shLoadedPlugin: String = loadValue(snapshot, "loadedPlugin")
        if (name != shName && !ctxt.suppressWarnings)
            log.warning { "name != snapshot name [$name !=  $shName]" }
        if (loadedPlugin != shLoadedPlugin && !ctxt.suppressWarnings)
            log.warning { "name != snapshot name [$loadedPlugin !=  $shLoadedPlugin]" }
        snapshotPlugin = loadValue(snapshot, "snapshotPlugin")

        val snapshotComponents: LinkedHashMap<String, Any> = loadValue(snapshot, "components")
        snapshotComponents.forEach { cName, cData -> components[cName]?.deserialize(ctxt, cData as LinkedHashMap<String, Any>) }
    }

    /**
     * {RU}
     * Обработка аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param context Контекст интерактивной консоли
     *
     * @return Результат обработки команд (true/false)
     * {RU}
     *
     * {EN}
     * Processing command line arguments.
     *
     * @param context context of interactive command line interface
     *
     * @return result of processing
     * {EN}
     */
    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true
        if (context.isNotEmpty())
            return find { it.command() == context.command() }?.process(context) == true
        return false
    }

    /**
     * {RU}
     * Настройка парсера аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param parent родительский парсер, к которому будут добавлены новые аргументы
     * @param useParent необходимость использования родительского парсера
     *
     * @return парсер аргументов
     * {RU}
     *
     * {EN}
     * Configuring parser for command line arguments. It is used to customize commands for this component/
     *
     * @param parent parent parser to which new commands will be added
     * @param useParent use parend parser
     *
     * @return argument parser
     * {EN}
     **/
    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent).apply {
                this@Component.forEach { it.configure(this) }
            }

    init {
        @Suppress("LeakingThis")
        parent?.add(this)
    }
}
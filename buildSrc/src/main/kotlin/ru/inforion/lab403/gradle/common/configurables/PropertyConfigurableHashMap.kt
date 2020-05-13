package ru.inforion.lab403.gradle.common.configurables

open class PropertyConfigurableHashMap :
        HashMap<String, Any?>(), IPropertyConfigurable {
    override fun propertyMissing(name: String, value: Any?) = set(name, value)
}
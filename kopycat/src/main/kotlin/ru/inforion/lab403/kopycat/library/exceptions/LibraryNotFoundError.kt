package ru.inforion.lab403.kopycat.library.exceptions

class LibraryNotFoundError(libraryName: String) : Exception("Can't find library $libraryName")
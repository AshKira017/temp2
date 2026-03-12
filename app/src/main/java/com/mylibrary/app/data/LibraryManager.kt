package com.mylibrary.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LibraryManager {

    private const val PREF_NAME = "mylibrary"
    private const val PREF_KEY = "mylib_v3"
    private val gson = Gson()
    private var books: MutableList<Book> = mutableListOf()
    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        load()
    }

    private fun load() {
        val json = prefs.getString(PREF_KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<Book>>() {}.type
        books = try { gson.fromJson(json, type) } catch (e: Exception) { mutableListOf() }
    }

    private fun save() {
        prefs.edit().putString(PREF_KEY, gson.toJson(books)).apply()
    }

    fun getBooks(): List<Book> = books.toList()

    fun getBook(id: String): Book? = books.find { it.id == id }

    fun hasBook(id: String): Boolean = books.any { it.id == id }

    fun addBook(book: Book) {
        if (!hasBook(book.id)) {
            books.add(book)
            save()
        }
    }

    fun removeBook(id: String) {
        books.removeAll { it.id == id }
        save()
    }

    fun saveChapter(bookId: String, chapter: Chapter) {
        val book = books.find { it.id == bookId } ?: return
        val idx = book.chapters.indexOfFirst { it.id == chapter.id }
        if (idx >= 0) book.chapters[idx] = chapter
        else book.chapters.add(chapter)
        save()
    }

    fun deleteChapter(bookId: String, chapterId: String) {
        books.find { it.id == bookId }?.chapters?.removeAll { it.id == chapterId }
        save()
    }

    fun exportJson(): String = gson.toJson(books)

    fun importJson(json: String): Int {
        val type = object : TypeToken<MutableList<Book>>() {}.type
        books = gson.fromJson(json, type)
        save()
        return books.size
    }

    fun clearAll() {
        books.clear()
        save()
    }
}

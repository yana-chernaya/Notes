package com.example.notes.data.repository

import com.example.notes.data.local.NoteDbModel
import com.example.notes.data.local.NotesDao
import com.example.notes.data.mapper.toContentItemDbModels
import com.example.notes.data.mapper.toDbModel
import com.example.notes.data.mapper.toEntities
import com.example.notes.data.mapper.toEntity
import com.example.notes.data.storage.ImageFileManager
import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import com.example.notes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager
) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        isPinned: Boolean,
        updatedAt: Long
    ) {
        val processedContent = content.processForStorage()
        val noteDbModel = NoteDbModel(
            id = 0,
            title = title,
            isPinned = isPinned,
            updatedAt = updatedAt
        )
        notesDao.addNoteWithContent(noteDbModel, processedContent)
    }

    override suspend fun deleteNote(noteId: Int) {
        val note = notesDao.getNote(noteId).toEntity()
        notesDao.deleteNote(noteId)

        note.content
            .filterIsInstance<ContentItem.Image>()
            .map { it.url }
            .forEach { url ->
                imageFileManager.deleteImage(url)
            }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(noteId = note.id).toEntity()

        val oldUrls = oldNote.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val newUrls = note.content.filterIsInstance<ContentItem.Image>().map { it.url }

        val removedUrls = oldUrls - newUrls

        removedUrls.forEach { url ->
            imageFileManager.deleteImage(url)
        }

        val processedContent = note.content.processForStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.updateNote(
            noteDbModel = processedNote.toDbModel(),
            content = processedContent.toContentItemDbModels(noteId = note.id)
        )
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map { it.toEntities() }
    }

    override suspend fun getNote(noteId: Int): Note {
        return notesDao.getNote(noteId).toEntity()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesDao.searchNotes(query).map { it.toEntities() }
    }

    override suspend fun switchPinnedStatus(noteId: Int) {
        notesDao.switchPinnedStatus(noteId)
    }

    private suspend fun List<ContentItem>.processForStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isInternal(contentItem.url)) {
                        contentItem
                    } else {
                        val internalPath =
                            imageFileManager.copyImageToInternalStorage(contentItem.url)
                        ContentItem.Image(internalPath)
                    }
                }

                is ContentItem.Text -> contentItem
            }
        }
    }
}












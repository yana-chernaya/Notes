package com.example.notes.data.mapper

import com.example.notes.data.local.ContentItemDbModel
import com.example.notes.data.local.ContentType
import com.example.notes.data.local.NoteDbModel
import com.example.notes.data.local.NoteWithContentDbModel
import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import kotlin.collections.map
import kotlin.collections.mapIndexed

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(id, title, isPinned, updatedAt)
}

fun NoteWithContentDbModel.toEntity(): Note {
    return Note(
        id = noteDbModel.id,
        title = noteDbModel.title,
        content = content.toContentItems(),
        isPinned = noteDbModel.isPinned,
        updatedAt = noteDbModel.updatedAt
    )
}

fun List<NoteWithContentDbModel>.toEntities(): List<Note> {
    return map { it.toEntity() }
}

fun List<ContentItem>.toContentItemDbModels(noteId: Int): List<ContentItemDbModel> {
    return mapIndexed { index, contentItem ->
        when (contentItem) {
            is ContentItem.Image -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.IMAGE,
                    content = contentItem.url,
                    order = index
                )
            }

            is ContentItem.Text -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.TEXT,
                    content = contentItem.content,
                    order = index
                )
            }
        }
    }
}

fun List<ContentItemDbModel>.toContentItems(): List<ContentItem> {
    return map { contentItem ->
        when (contentItem.contentType) {
            ContentType.IMAGE -> {
                ContentItem.Image(url = contentItem.content)
            }

            ContentType.TEXT -> {
                ContentItem.Text(content = contentItem.content)
            }
        }
    }
}
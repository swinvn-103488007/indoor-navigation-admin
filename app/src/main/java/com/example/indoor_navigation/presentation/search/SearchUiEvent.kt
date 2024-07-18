package com.example.indoor_navigation.presentation.search

sealed class SearchUiEvent{
    object SearchSuccess: SearchUiEvent()
    object SearchInvalid: SearchUiEvent()
}

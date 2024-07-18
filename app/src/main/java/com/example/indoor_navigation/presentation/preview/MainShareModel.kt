package com.example.indoor_navigation.presentation.preview

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.indoor_navigation.data.model.Record
import com.example.indoor_navigation.domain.hit_test.HitTestResult
import com.example.indoor_navigation.domain.repository.RecordsRepository
import com.example.indoor_navigation.domain.tree.Tree
import com.example.indoor_navigation.domain.tree.TreeNode
import com.example.indoor_navigation.domain.use_cases.FindWay
import com.example.indoor_navigation.presentation.LabelObject
import com.example.indoor_navigation.presentation.confirmer.ConfirmFragment
import com.example.indoor_navigation.presentation.preview.state.PathState
import com.example.indoor_navigation.presentation.search.SearchFragment
import com.example.indoor_navigation.presentation.search.SearchUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import io.github.sceneview.ar.arcore.ArFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainShareModel @Inject constructor(
    private val tree: Tree,
    private val findWay: FindWay,
    private val records: RecordsRepository
): ViewModel() {

    private var _pathStateFlow = MutableStateFlow(PathState())
    val pathState = _pathStateFlow.asStateFlow()

    private var _mainUiEventsFlow = MutableSharedFlow<MainUiEvent>()
    val mainUiEvents = _mainUiEventsFlow.asSharedFlow()

    private var _searchUiEventsFlow = MutableSharedFlow<SearchUiEvent>()
    val searchUiEvents = _searchUiEventsFlow.asSharedFlow()

    private var _frame = MutableStateFlow<ArFrame?>(null)
    val frame = _frame.asStateFlow()

    private var _timeRecords = MutableStateFlow<List<Record>>(listOf())
    val timeRecords = _timeRecords.asStateFlow()

    private var _selectedNodeFlow = MutableStateFlow<TreeNode?>(null)
    val selectedNode = _selectedNodeFlow.asStateFlow()

    private var _linkPlacementModeFlow = MutableStateFlow(false)
    val linkPlacementMode = _linkPlacementModeFlow.asStateFlow()

    private var pathFindJob: Job? = null
    private var recordsJob: Job? = null

    val treeDiffUtils = tree.diffUtils
    val entriesNumber = tree.getEntriesNumbers()

    @SuppressLint("StaticFieldLeak")
    private var _confirmationObjectFlow = MutableStateFlow<LabelObject?>(null)
    val confirmationObject = _confirmationObjectFlow.asStateFlow()

    init {
        preload()
    }

    fun onEvent(ev: MainEvent) {
        when (ev){
            is MainEvent.NewFrame -> {
                viewModelScope.launch {
                    _frame.emit(ev.frame)
                }
            }
            is MainEvent.NewConfirmationObject -> {
                _confirmationObjectFlow.update { ev.confObject }
            }
            is MainEvent.TrySearch -> {
                viewModelScope.launch {
                    processSearch(ev.number, ev.changeType)
                }
            }
            is MainEvent.AcceptConfObject -> {
                when (ev.confirmType) {
                    ConfirmFragment.CONFIRM_INITIALIZE -> {
                        viewModelScope.launch {
                            _confirmationObjectFlow.value?.let {
                                initialize(
                                    it.label,
                                    it.pos.position,
                                    it.pos.orientation
                                )
                                _confirmationObjectFlow.update { null }

                            }
                        }
                    }
                    ConfirmFragment.CONFIRM_ENTRY -> {
                        viewModelScope.launch {
                            _confirmationObjectFlow.value?.let {
                                createNode(
                                    number = it.label,
                                    position = it.pos.position,
                                    orientation = it.pos.orientation
                                )
                                _confirmationObjectFlow.update { null }

                            }
                        }
                    }
                }
            }
            is MainEvent.RejectConfObject -> {
                viewModelScope.launch {
                    _confirmationObjectFlow.update { null }
                }
            }
            is MainEvent.NewSelectedNode -> {
                viewModelScope.launch {
                    _selectedNodeFlow.update { ev.node }
                }
            }
            is MainEvent.LoadRecords -> {
                viewModelScope.launch {
                    loadRecords()
                }
            }
            is MainEvent.ChangeLinkMode -> {
                viewModelScope.launch {
                    _linkPlacementModeFlow.update { !linkPlacementMode.value }
                }
            }
            is MainEvent.CreateNode -> {
                viewModelScope.launch {
                    createNode(
                        number = ev.number,
                        position = ev.position,
                        orientation = ev.orientation,
                        hitTestResult = ev.hitTestResult
                    )
                }
            }
            is MainEvent.LinkNodes -> {
                viewModelScope.launch {
                    linkNodes(ev.node1, ev.node2)
                }
            }
            is MainEvent.DeleteNode -> {
                viewModelScope.launch {
                    removeNode(ev.node)
                }
            }
        }
    }

    private fun getCurrentWeekTime(): Long {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        return (dayOfWeek-1)*24*60*60*1000L +
                hour*60*60*1000L +
                minutes*60*1000L
    }

    private suspend fun processSearch(number: String, changeType: Int) {
        val entry = tree.getEntry(number)
        if (entry == null) {
            _searchUiEventsFlow.emit(SearchUiEvent.SearchInvalid)
            return
        } else {

            if (changeType == SearchFragment.TYPE_START) {
                val endEntry = pathState.value.endEntry
                _pathStateFlow.update {
                    PathState(
                        startEntry = entry,
                        endEntry = if (entry.number == endEntry?.number) null else endEntry
                 )
                }
            } else {
                val startEntry = pathState.value.startEntry
            _pathStateFlow.update {
                PathState(
                    startEntry = if (entry.number == startEntry?.number) null else startEntry,
                    endEntry = entry
                )
            }
        }
        //The search ended successfully
        pathFindJob?.cancel()
        pathFindJob = viewModelScope.launch {
            pathFind()
        }
        _searchUiEventsFlow.emit(SearchUiEvent.SearchSuccess)

         //saving route to the database
        pathState.value.startEntry?.let { start ->
            pathState.value.endEntry?.let { end ->
                val record = Record(
                    start = start.number,
                    end = end.number,
                    time = getCurrentWeekTime()
                )
                records.insertRecord(record)
            }
        }

    }
    }

    private suspend fun pathFind(){
        val from = pathState.value.startEntry?.number ?: return
        val to = pathState.value.endEntry?.number ?: return
        if (tree.getEntry(from) != null && tree.getEntry(to) != null) {
            val path = findWay(from, to, tree)
            if (path != null) {
                _pathStateFlow.update { it.copy(
                    path = path
                ) }
            } else {
                _mainUiEventsFlow.emit(MainUiEvent.PathNotFound)
            }
        }
        else {
            throw Exception("Unknown tree nodes")
        }
    }

    private suspend fun createNode(
        number: String? = null,
        position: Float3? = null,
        orientation: Quaternion? = null,
        hitTestResult: HitTestResult? = null,
    ) {
        if (position == null && hitTestResult == null){
            throw Exception("No position was provided")
        }
     //   if (position == null) {
        if (number != null && tree.hasEntry(number)){
            _mainUiEventsFlow.emit(MainUiEvent.EntryAlreadyExists)
            return
        }
        val treeNode = tree.addNode(
            position ?: hitTestResult!!.orientatedPosition.position,
            number,
            orientation
        )

        treeNode.let {
            if (number != null){
                _mainUiEventsFlow.emit(MainUiEvent.EntryCreated)
            }
            _mainUiEventsFlow.emit(MainUiEvent.NodeCreated(
                treeNode,
                hitTestResult?.hitResult?.createAnchor()
            ))
        }
    }

    private suspend fun removeNode(node: TreeNode){
        tree.removeNode(node)
        _mainUiEventsFlow.emit(MainUiEvent.NodeDeleted(node))
        if (node == selectedNode.value) {_selectedNodeFlow.update { null }}
        if (node == pathState.value.endEntry) {_pathStateFlow.update { it.copy(endEntry = null, path = null) }}
        else if (node == pathState.value.startEntry) {_pathStateFlow.update { it.copy(startEntry = null, path = null) }}
    }

    private suspend fun linkNodes(node1: TreeNode, node2: TreeNode){
        if (tree.addLink(node1, node2)) {
            _linkPlacementModeFlow.update { false }
            _mainUiEventsFlow.emit(MainUiEvent.LinkCreated(node1, node2))
        }
    }

    private suspend fun loadRecords() {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            val time = getCurrentWeekTime() + 30*60*1000L
            records.getRecords(time, 5).collectLatest{ records ->
                _timeRecords.emit(records)
            }
        }
    }

    private fun preload(){
        viewModelScope.launch {
            tree.preload()
        }
    }

    private suspend fun initialize(entryNum: String, pos: Float3, newOrientation: Quaternion): Boolean {
        var result: Result<Unit?>
        withContext(Dispatchers.IO) {
            result = tree.initialize(entryNum, pos, newOrientation)
        }
        if (result.isFailure){
            _mainUiEventsFlow.emit(MainUiEvent.InitFailed(
                result.exceptionOrNull() as java.lang.Exception?
            ))
            return false
        }
        _mainUiEventsFlow.emit(MainUiEvent.InitSuccess)
        val entry = tree.getEntry(entryNum)
        if (entry != null){
            _pathStateFlow.update { PathState(
                startEntry = entry
            ) }
        }
        else {
            _pathStateFlow.update { PathState() }
        }
        return true
    }
}

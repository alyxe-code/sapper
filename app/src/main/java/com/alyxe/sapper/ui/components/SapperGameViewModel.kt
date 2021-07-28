package com.alyxe.sapper.ui.components

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class SapperGameViewModel : ViewModel() {

    private var _grid = MutableStateFlow<Map<Pair<Int, Int>, SapperField>>(emptyMap())
    val grid = _grid.asStateFlow()

    private val _state = MutableStateFlow(State.Init)
    val state = _state.asStateFlow()

    var config = GridBuilder.Config(width = 5, height = 5, bombCount = 5)
        private set

    fun onLevelSelected(config: GridBuilder.Config) {
        this.config = config

        _grid.value = GridBuilder.buildSapperFields(config)

        _state.value = State.Playing
    }

    fun onFieldSelected(x: Int, y: Int) {
        if (_state.value != State.Playing) return

        val field = _grid.value[x to y] ?: return
        if (field !is SapperField.Closed) return

        _grid.value = _grid.value
            .toMutableMap()
            .apply { this[x to y] = field.next }
            .toMap()

        if (_grid.value.values.none { (it as? SapperField.Closed)?.next is SapperField.Open }) {
            _state.value = State.Win
            _grid.value = _grid.value
                .toMutableMap()
                .map { (position, value) ->
                    val updated = SapperField.DeactivatedBomb
                        .takeIf { (value as? SapperField.Closed)?.next is SapperField.Bomb }
                        ?: value

                    position to updated
                }
                .toMap()

            return
        }

        when (field.next) {
            SapperField.Bomb -> _state.value = State.GameOver
            is SapperField.Open -> onFieldOpened(x, y)
            is SapperField.Closed -> throw IllegalStateException()
            SapperField.DeactivatedBomb -> Unit
        }
    }

    fun onRestart() {
        _state.value = State.Init
    }

    private fun onFieldOpened(x: Int, y: Int) {
        val field = _grid.value[x to y]

        if (field !is SapperField.Open || field.countBombs != 0) return

        for (i in (x - 1)..(x + 1)) {
            for (j in (y - 1)..(y + 1)) {
                val actual = _grid.value[i to j] ?: continue
                if (actual !is SapperField.Closed) continue
                if (actual.next !is SapperField.Open) continue
                if (actual.next.countBombs != 0) continue

                _grid.value = _grid.value
                    .toMutableMap()
                    .apply { this[i to j] = actual.next }
                    .toMap()

                onFieldOpened(i, j)
            }
        }
    }

    enum class State {
        Init,
        Playing,
        GameOver,
        Win,
    }
}

object GridBuilder {

    class Config(
        val width: Int,
        val height: Int,
        val bombCount: Int,
    )

    fun buildSapperFields(config: Config): Map<Pair<Int, Int>, SapperField> {
        val map: MutableMap<Pair<Int, Int>, SapperField?> = (0 until config.width)
            .flatMap { x -> (0 until config.height).map { (x to it) to null } }
            .toMap()
            .toMutableMap()

        return map
            .apply {
                repeat(config.bombCount) { addRandomBomb() }
                fillWithUsualFields()
            }
            .mapNotNull { (key, value) -> key to (value ?: return@mapNotNull null) }
            .toMap()
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.addRandomBomb() {
        val width = keys
            .map { it.first }
            .maxOf { it }
            .plus(1)

        val height = keys
            .map { it.second }
            .maxOf { it }
            .plus(1)

        val x = Random
            .nextInt(from = 0, until = width)
            .also { println("x = $it") }
            .also { assert(it in 0 until height) }

        val position = keys
            .filter { it.first == x }
            .also { println(it) }
            .filter { this[it] == null }
            .also { println(it) }
            .takeIf { it.isNotEmpty() }
            ?.random()
            .also { println("new position $it") }
            ?: throw AssertionError()

        this[position] = SapperField.Closed(SapperField.Bomb)
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.fillWithUsualFields() {
        keys.forEach {
            if (this[it] == null) {
                addUsualField(it)
            }
        }
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.addUsualField(position: Pair<Int, Int>) {
        var countBombsAround = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val field = this[(position.first + i) to (position.second + j)]
                if (field is SapperField.Bomb) {
                    countBombsAround++
                } else if ((field as? SapperField.Closed)?.next is SapperField.Bomb) {
                    countBombsAround++
                }
            }
        }

        this[position] = SapperField.Closed(SapperField.Open(countBombsAround))
    }
}
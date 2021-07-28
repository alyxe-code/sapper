package com.alyxe.sapper

import com.alyxe.sapper.ui.components.SapperField
import org.junit.Test
import kotlin.random.Random

class SapperTest {
    @Test
    fun generateGrid() {

    }

    @Test
    fun generateInitial() {
        val initialGrid = initial(WIDTH, HEIGHT)

        assert(initialGrid.keys.size == WIDTH * HEIGHT)
    }

    @Suppress("SameParameterValue")
    private fun initial(width: Int, height: Int): Map<Pair<Int, Int>, SapperField?> {
        return (0 until width).flatMap { x ->
            (0 until height).map { y ->
                (x to y) to null
            }
        }.toMap()
    }

    @Test
    fun generateBombs() {
        val map = initial(WIDTH, HEIGHT)
            .toMutableMap()
            .apply { withBombs(BOMBS_COUNT) }

        val generatedBombsCount = map
            .filter { (it.value as? SapperField.Closed)?.next is SapperField.Bomb }
            .size

        assert(generatedBombsCount == BOMBS_COUNT)
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.withBombs(count: Int) {
        repeat(count) { addBomb() }
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.addBomb() {
        val x = Random
            .nextInt(from = 0, until = WIDTH)
            .also { println("x = $it") }
            .also { assert(it in 0 until WIDTH) }

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

    @Test
    fun usualFields() {
        val initial = initial(width = WIDTH, height = HEIGHT)
        initial.toMutableMap().run {
            fillWithUsualFields()
            assert(keys.all { (this[it] as? SapperField.Closed)?.next is SapperField.Open })
            assert(keys.all { ((this[it] as? SapperField.Closed)?.next as? SapperField.Open)?.countBombsAround == 0 })
            println(this)
        }

        initial.toMutableMap().run {
            withBombs(BOMBS_COUNT)
            fillWithUsualFields()
            assert(keys.count {
                (this[it] as? SapperField.Closed)?.next is SapperField.Bomb
            } == BOMBS_COUNT)
            println(this)
        }
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.fillWithUsualFields() {
        keys.forEach {
            if (this[it] == null) {
                addEmptyField(it.first, it.second)
            }
        }
    }

    private fun MutableMap<Pair<Int, Int>, SapperField?>.addEmptyField(x: Int, y: Int) {
        var countBombsAround = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val field = this[(x + i) to (y + j)]
                if (field is SapperField.Bomb) {
                    countBombsAround++
                } else if ((field as? SapperField.Closed)?.next is SapperField.Bomb) {
                    countBombsAround++
                }
            }
        }

        this[x to y] = SapperField.Closed(SapperField.Open(countBombsAround))
    }

    companion object {
        private const val WIDTH = 5
        private const val HEIGHT = 5
        private const val BOMBS_COUNT = 6
    }
}
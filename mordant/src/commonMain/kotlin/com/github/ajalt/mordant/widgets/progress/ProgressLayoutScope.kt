package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import kotlin.time.ComparableTimeMark

internal const val TEXT_FPS = 5
internal const val ANIMATION_FPS = 30

class TaskId

// TODO: docs on all of these
interface ProgressLayoutScope<T> {
    /**
     * Add a cell to this layout.
     *
     * The [builder] will be called every time the cell is rendered. In the case of
     * animations, that will be at its [fps].
     *
     * @param width The width of the cell.
     * @param fps The number of times per second to refresh the cell when animated. If 0, the cell will not be refreshed.
     * @param align The text alignment for the cell when multiple tasks are present and cells are aligned.
     * @param builder A lambda returning the widget to display in this cell.
     */
    fun cell(
        width: ColumnWidth = ColumnWidth.Auto,
        fps: Int = TEXT_FPS,
        align: TextAlign? = null,
        builder: ProgressState<T>.() -> Widget,
    )

    /** The default framerate for text based cells */
    val textFps: Int

    /** The default framerate for animation cells */
    val animationFps: Int
}

data class ProgressBarCell<T>(
    val columnWidth: ColumnWidth = ColumnWidth.Auto,
    val fps: Int = TEXT_FPS,
    val align: TextAlign? = null,
    val content: ProgressState<T>.() -> Widget,
) {
    init {
        require(fps >= 0) { "fps cannot be negative" }
    }
}

/**
 * The cells and configuration for a progress bar layout.
 */
class ProgressBarDefinition<T>(
    /**
     * The cells in this layout.
     */
    val cells: List<ProgressBarCell<T>>,

    /**
     * The spacing between cells in this layout.
     */
    val spacing: Int,

    /**
     * How to align the columns of the progress bar when multiple tasks are present.
     */
    val alignColumns: Boolean,
)

fun <T> ProgressBarDefinition<T>.build(
    vararg states: ProgressState<T>,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): Widget {
    return maker.build(this, states.asList())
}

fun <T> ProgressBarDefinition<T>.build(
    context: T,
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): Widget {
    val state = ProgressState(
        context, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
    )
    return build(state, maker = maker)
}

// TODO test these, add docs
fun ProgressBarDefinition<Unit>.build(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): Widget {
    val state = ProgressState(
        Unit, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
    )
    return build(state, maker = maker)
}

// TODO: docs
fun <T> progressBarContextLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    textFps: Int = TEXT_FPS,
    animationFps: Int = ANIMATION_FPS,
    init: ProgressLayoutScope<T>.() -> Unit,
): ProgressBarDefinition<T> {
    return BaseProgressLayoutScope<T>(textFps, animationFps)
        .apply(init)
        .build(spacing, alignColumns)
}

fun progressBarLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    textFps: Int = TEXT_FPS,
    animationFps: Int = ANIMATION_FPS,
    init: ProgressLayoutScope<Unit>.() -> Unit,
): ProgressBarDefinition<Unit> {
    return progressBarContextLayout(spacing, alignColumns,textFps, animationFps, init)
}


class BaseProgressLayoutScope<T>(
    override val textFps: Int = TEXT_FPS,
    override val animationFps: Int = ANIMATION_FPS,
) : ProgressLayoutScope<T> {
    private val cells: MutableList<ProgressBarCell<T>> = mutableListOf()

    override fun cell(
        width: ColumnWidth,
        fps: Int,
        align: TextAlign?,
        builder: ProgressState<T>.() -> Widget,
    ) {
        cells += ProgressBarCell(width, fps, align, builder)
    }

    fun build(spacing: Int, alignColumns: Boolean): ProgressBarDefinition<T> {
        return ProgressBarDefinition(cells, spacing, alignColumns)
    }
}
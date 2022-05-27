package com.ildango.capstone.mypages.mywishlist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.VectorDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.ildango.capstone.R

class SwipeItemCallback(private val context: Context, private val adapter: MyWishListAdapter) : ItemTouchHelper.Callback() {

    private var curPos:Int ?= null
    private var prePos:Int ?= null
    private var curDx = 0f
    private var clamp = 0f
    private var isClamped = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(UP or DOWN, LEFT or RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {

            val itemView = viewHolder.itemView
            val newX = clampViewPositionHorizontal(dX, isCurrentlyActive)
            val height: Float = itemView.bottom.toFloat() - itemView.top.toFloat()
            val width: Float = height / 3

            // 빨간 박스 및 삭제 아이콘
            val p = Paint()
            p.color = ContextCompat.getColor(context, R.color.deleteButton)
            val bg = RectF(
                itemView.left.toFloat() + width,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            c.drawRoundRect(bg, 40F, 40F, p)

            val icon: Bitmap =
                (context.getDrawable(R.drawable.ic_baseline_delete_24) as VectorDrawable).toBitmap()
            val iconDest = RectF(
                itemView.right.toFloat() - 2 * width,
                itemView.top.toFloat() + width,
                itemView.right.toFloat() - width,
                itemView.bottom.toFloat() - width
            )

            c.drawBitmap(icon, null, iconDest, p)

            // swipe 고정
            if (newX == -clamp) {
                itemView.animate().translationX(-clamp).setDuration(100L).start()
                return
            }

            curDx = newX
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                itemView,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue*10

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        isClamped = curDx<=-clamp
        return 2f
    }

    private fun clampViewPositionHorizontal(
        dX:Float,
        isCurrentlyActive: Boolean
    ): Float {
        val max = 0f
        val newX:Float = if(isClamped) {
            if (isCurrentlyActive)
                if (dX < 0) dX / 3 - clamp
                else dX - clamp
            else -clamp
        }
        else dX / 2

        return if(newX<max) newX else max
    }

    fun setClamp(clamp:Float) {this.clamp = clamp}

    fun removePreviousClamp(recyclerView: RecyclerView) {
        if(curPos==prePos) return

        prePos?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            viewHolder.itemView.animate().x(0f).setDuration(100L).start()
            isClamped = false
            prePos = null
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        curDx = 0f
        prePos = viewHolder.bindingAdapterPosition
        getDefaultUIUtil().clearView(viewHolder.itemView)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            curPos = viewHolder.bindingAdapterPosition
            getDefaultUIUtil().onSelected(it.itemView)
        }
    }
}
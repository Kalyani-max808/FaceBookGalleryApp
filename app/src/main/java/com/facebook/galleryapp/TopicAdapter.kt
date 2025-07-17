package com.facebook.galleryapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView


class TopicAdapter(private val ctx: Context, private val act: FragmentActivity?, val appInterfaces: AppInterfaces) : RecyclerView.Adapter<TopicAdapter.ViewHolder>() {

    val mItemCount = ItemDataset.items.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(ctx).inflate(R.layout.topic_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mItemCount
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = ItemDataset.items[position].topic_title
        holder.view.findViewById<TextView>(R.id.tvTopicTitle3).text = title
        val iconPath = AppUtils().getTopicImagePath(position).toString()
        holder.view.findViewById<ImageView>(R.id.imgTopicIcon).setImageURI(iconPath.toUri())
        holder.view.setOnClickListener {
            ItemDataset.item_current = ItemDataset.items.elementAt(position)
            if (act is AppInterfaces) {
                AdObject.admob?.loadNextScreen { appInterfaces.loadMenus() }
            }
        }
    }
}

/*---------------------ITEM DECORATION FOR THE RECYCLER VIEW ITEM--------------------*/
class CustomItemDecoration(val spacing: Int = 5, val includeEdge: Boolean = true) : RecyclerView.ItemDecoration() {

    val offset = 10.0f
    var paintCyan: Paint = Paint(Color.CYAN).apply {
        style = Paint.Style.STROKE
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // super.getItemOffsets(outRect, view, parent, state)

        if (parent.layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
            val layoutManager = parent.layoutManager as androidx.recyclerview.widget.GridLayoutManager
            val spanCount = layoutManager.spanCount
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }

        }


    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val lm = parent.layoutManager

        for (i in 0.until(parent.childCount)) {


            val child = parent.getChildAt(i)
            val parms = child.layoutParams as RecyclerView.LayoutParams
            val left: Float = (child.right + parms.rightMargin).toFloat()
            val right = child.left + offset
//            c!!.drawRect(
//
//                    left,child.top+offset,right,child.bottom+offset, paintCyan
//
//            )
        }
    }


}

class BoundaryItemDecoration(private val context: Context, color: Int, private val dividerHeight: Int = 5, val spacing: Int = 5, val includeEdge: Boolean = true) : RecyclerView.ItemDecoration() {
    private val paint: Paint = Paint()

    private var layoutOrientation = -1

    init {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dividerHeight.toFloat()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // super.getItemOffsets(outRect, view, parent, state)

        if (parent.layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
            val layoutManager = parent.layoutManager as androidx.recyclerview.widget.GridLayoutManager
            val spanCount = layoutManager.spanCount
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }

        }


    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (parent.layoutManager is androidx.recyclerview.widget.LinearLayoutManager && layoutOrientation == -1) {
            layoutOrientation = (parent.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).orientation
        }
        if (layoutOrientation == androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL) {
            horizontal(c, parent)
        } else {
            vertical(c, parent)
        }
    }

    private fun horizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val itemCount = parent.childCount
        for (i in 0 until itemCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = child.left + dividerHeight
            c.drawRect(left.toFloat(), (child.top + dividerHeight).toFloat(), right.toFloat(), (child.bottom + dividerHeight).toFloat(), paint)

        }
    }

    private fun vertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = child.top + dividerHeight
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}

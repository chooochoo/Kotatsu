package org.koitharu.kotatsu.reader.ui.thumbnails

import android.view.ViewGroup
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.PixelSize
import coil.size.Size
import kotlinx.android.synthetic.main.item_page_thumb.*
import kotlinx.coroutines.*
import org.koin.core.component.inject
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.list.BaseViewHolder
import org.koitharu.kotatsu.core.model.MangaPage
import org.koitharu.kotatsu.local.data.PagesCache
import org.koitharu.kotatsu.utils.ext.IgnoreErrors

class PageThumbnailHolder(parent: ViewGroup, private val scope: CoroutineScope) :
	BaseViewHolder<MangaPage, PagesCache>(parent, R.layout.item_page_thumb) {

	private var job: Job? = null
	private val thumbSize: Size
	private val coil by inject<ImageLoader>()

	init {
		val width = itemView.context.resources.getDimensionPixelSize(R.dimen.preferred_grid_width)
		thumbSize = PixelSize(
			width = width,
			height = (width * 13f / 18f).toInt()
		)
	}

	override fun onBind(data: MangaPage, extra: PagesCache) {
		imageView_thumb.setImageDrawable(null)
		textView_number.text = (bindingAdapterPosition + 1).toString()
		job?.cancel()
		job = scope.launch(Dispatchers.IO + IgnoreErrors) {
			val url = data.preview ?: data.url.let {
				val pageUrl = data.source.repository.getPageFullUrl(data)
				extra[pageUrl]?.toUri()?.toString() ?: pageUrl
			}
			val drawable = coil.execute(
				ImageRequest.Builder(context)
					.data(url)
					.size(thumbSize)
					.build()
			).drawable
			withContext(Dispatchers.Main.immediate) {
				imageView_thumb.setImageDrawable(drawable)
			}
		}
	}

	override fun onRecycled() {
		job?.cancel()
		imageView_thumb.setImageDrawable(null)
	}
}
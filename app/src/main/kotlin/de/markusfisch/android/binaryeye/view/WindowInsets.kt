package de.markusfisch.android.binaryeye.view

import android.graphics.Rect
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.WindowInsets

private var toolbarHeight = 0
fun setPaddingFromWindowInsets(mainLayout: View, toolbar: Toolbar, navbar: View?) {
	toolbarHeight = toolbar.layoutParams.height
	// SDK 35+ is edge-to-edge by default, so the toolbar needs to extend
	// below the status bar.
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
		return
	}
	mainLayout.setOnApplyWindowInsetsListener { _, insets ->
		val systemBarInsets = insets.getInsets(
			WindowInsets.Type.systemBars()
		)

		val statusBarTop = systemBarInsets.top
		if (toolbar.paddingTop == 0 && statusBarTop > 0) {
			toolbar.setPadding(0, statusBarTop, 0, 0)
			toolbar.layoutParams.height += statusBarTop
		}

		val navBarBottom = systemBarInsets.bottom
		if (navBarBottom > 0) {
			mainLayout.setPadding(0, 0, 0, navBarBottom);
			navbar?.apply {
				layoutParams.height = navBarBottom
			}
		}

		insets
	}
}

fun View.setPaddingFromWindowInsets() {
	doOnApplyWindowInsets { v, insets -> v.setPadding(insets) }
}

// A slight variation of the idea from Google's Chris Banes to avoid
// adding the toolbar height for every view that needs to be inset.
// For the original post see here:
// https://medium.com/androiddevelopers/windowinsets-listeners-to-layouts-8f9ccc8fa4d1
fun View.doOnApplyWindowInsets(f: (View, Rect) -> Unit) {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
		f(this, insetsWithToolbar())
	} else {
		ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
			f(v, insetsWithToolbar(insets))
			insets
		}
		// It's important to explicitly request the insets (again) in
		// case the view was created in Fragment.onCreateView() because
		// setOnApplyWindowInsetsListener() won't fire when the view
		// isn't attached.
		requestApplyInsetsWhenAttached()
	}
}

private fun insetsWithToolbar(insets: WindowInsetsCompat? = null) = Rect(
	insets?.systemWindowInsetLeft ?: 0,
	(insets?.systemWindowInsetTop ?: 0) + toolbarHeight,
	insets?.systemWindowInsetRight ?: 0,
	insets?.systemWindowInsetBottom ?: 0
)

@RequiresApi(Build.VERSION_CODES.KITKAT)
private fun View.requestApplyInsetsWhenAttached() {
	if (isAttachedToWindow) {
		ViewCompat.requestApplyInsets(this)
	} else {
		addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
			override fun onViewAttachedToWindow(v: View) {
				v.removeOnAttachStateChangeListener(this)
				ViewCompat.requestApplyInsets(v)
			}

			override fun onViewDetachedFromWindow(v: View) = Unit
		})
	}
}

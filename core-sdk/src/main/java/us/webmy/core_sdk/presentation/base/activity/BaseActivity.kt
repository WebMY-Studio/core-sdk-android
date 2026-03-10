package us.webmy.core_sdk.presentation.base.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.viewbinding.ViewBinding
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import org.koin.androidx.scope.ScopeActivity
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : ScopeActivity() {

    protected abstract val viewModel: VM

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB by lazy {
        val bindingClass = findBindingClass()
        val inflateMethod = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        inflateMethod.invoke(null, layoutInflater) as VB
    }

    @Suppress("UNCHECKED_CAST")
    private fun findBindingClass(): Class<VB> {
        val genericSuperclass = javaClass.genericSuperclass
        if (genericSuperclass is ParameterizedType) {
            return genericSuperclass.actualTypeArguments[1] as Class<VB>
        }
        throw kotlin.IllegalArgumentException("Cannot find binding class for ${javaClass.simpleName}")
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        setContentView(binding.root)
        observe(viewModel)
        initView()
    }

    abstract fun initView()

    abstract fun observe(viewModel: VM)

    protected fun overrideOnBackPressed(action: () -> Unit) {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action()
            }
        })
    }
}
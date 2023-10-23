package com.moaview.moaview.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

/**
 *  ex. class AFragment : BaseFragment<BINDING>(BINDING::inflate)
 *  onCreateView() 생략
 *  onViewCreated() 활용 각 fragment init 등 처리
 * */
abstract class BaseFragment<T: ViewBinding>(
    private val inflate: Inflate<T>
) : Fragment() {

    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        initFragment()
//    }

//    protected open fun initFragment() {}
}

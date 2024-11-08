package com.example.h2h

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CoverAvtBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cover_avt_sheet, container, false)

        view.findViewById<TextView>(R.id.option_view_cover).setOnClickListener {
            // Xử lý khi chọn "Xem hồ sơ"
            dismiss()
        }

        view.findViewById<TextView>(R.id.option_change_cover).setOnClickListener {
            // Xử lý khi chọn "Thay đổi ảnh đại diện"
            dismiss()
        }
        view.findViewById<TextView>(R.id.option_takecover).setOnClickListener {
            // Xử lý khi chọn "Thay đổi ảnh đại diện"
            dismiss()
        }

        view.findViewById<TextView>(R.id.option_cancel).setOnClickListener {
            // Xử lý khi chọn "Hủy"
            dismiss()
        }

        return view
    }
}
package com.example.h2h

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AvtBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.avt_bottom_sheet, container, false)

        view.findViewById<TextView>(R.id.option_view_profile).setOnClickListener {
            // Xử lý khi chọn "Xem hồ sơ"
            dismiss()
        }

        view.findViewById<TextView>(R.id.option_change_avatar).setOnClickListener {
            // Xử lý khi chọn "Thay đổi ảnh đại diện"
            dismiss()
        }
        view.findViewById<TextView>(R.id.option_takeavt).setOnClickListener {
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
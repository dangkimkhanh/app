package com.example.h2h.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.adapter.ListUserAdapter
import com.example.h2h.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MachFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.match_fragment, container, false)
        val filterButton = view.findViewById<ImageView>(R.id.match_filter)
        val recyclerView = view.findViewById<RecyclerView>(R.id.comment_recycler_view)
        val listUserAdapter = ListUserAdapter()
        recyclerView.adapter = listUserAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    listUserAdapter.loadMoreUsers() // Tải thêm dữ liệu khi lướt đến cuối
                }
            }
        })
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        usersRef.limitToFirst(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { users.add(it) }
                }
                listUserAdapter.updateUserList(users)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })
        filterButton.setOnClickListener {
            // Hiển thị bộ lọc bằng AlertDialog
            val filterLayout = inflater.inflate(R.layout.filter_layout, null)
            val ageRangeSlider = filterLayout.findViewById<com.google.android.material.slider.RangeSlider>(R.id.age_range_slider)
            val genderRadioGroup = filterLayout.findViewById<RadioGroup>(R.id.gender_radio_group)

            // Thiết lập giá trị mặc định cho RangeSlider (nếu cần)
            ageRangeSlider.setValues(15f, 30f) // Giá trị mặc định: 20 đến 40 tuổi
            val ageRangeText = filterLayout.findViewById<TextView>(R.id.age_range_text)
            ageRangeText.text = "${ageRangeSlider.values[0].toInt()}-${ageRangeSlider.values[1].toInt()}"
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext()) // Sửa lỗi tại đây
                .setTitle("Bộ lọc") // Thêm tiêu đề cho AlertDialog
                .setView(filterLayout)
                .setPositiveButton("Áp dụng") { dialog, _ ->
                    // Lấy giá trị từ thanh kéo chọn độ tuổi
                    val minAge = ageRangeSlider.values[0].toInt()
                    val maxAge = ageRangeSlider.values[1].toInt()

                    // Lấy giá trị từ tùy chọn giới tính
                    val selectedGenderId = genderRadioGroup.checkedRadioButtonId
                    val selectedGender = when (selectedGenderId) {
                        R.id.male_radio_button -> "Nam"
                        R.id.female_radio_button -> "Nữ"
                        R.id.all_radio_button -> "Cả hai"
                        else -> "Cả hai" // Giá trị mặc định nếu không chọn giới tính
                    }
                    handleFilterResults(minAge, maxAge, selectedGender)
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
            ageRangeSlider.addOnChangeListener { slider, value, fromUser ->
                val minAge = slider.values[0].toInt()
                val maxAge = slider.values[1].toInt()
                val ageRangeText = filterLayout.findViewById<TextView>(R.id.age_range_text)
                ageRangeText.text = "$minAge-$maxAge"
            }
        }


        return view
    }

    // Hàm xử lý kết quả
    private fun handleFilterResults(minAge: Int, maxAge: Int, gender: String) {
        val ageRangeText = view?.findViewById<TextView>(R.id.age_range_text)
        ageRangeText?.text = "$minAge-$maxAge"

        // Thực hiện logic tiếp theo...
    }

}
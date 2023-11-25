package com.example.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.ProfileUpdateFragment
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentMyPageBinding
import com.example.find_my_matzip.model.FollowerDto
import com.example.find_my_matzip.model.FollowingDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.adapter.BoardRecyclerAdapter

import com.example.find_my_matzip.navTab.adapter.ProfileAdapter
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyPageFragment : Fragment() {
    lateinit var binding: FragmentMyPageBinding
    lateinit var adapter: ProfileAdapter
    lateinit var boardAdapter: BoardRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(layoutInflater, container, false)
        // 보드 어댑터
        boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, emptyList())
        binding.boardRecyclerView.adapter = boardAdapter
        // 팔로잉




        binding.updateBtn.setOnClickListener {
//            profileUpdateFragment 회원수정창(타 프레그먼트로) 이동하는 코드
            val profileUpdateFragment = ProfileUpdateFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, profileUpdateFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }


        //로그인 정보
        val loginInfo = SharedPreferencesManager.getLoginInfo() ?: emptyMap()
        val userId = loginInfo["id"]


        val userService = (context?.applicationContext as MyApplication).userService
        val profileList = userService.getProfile(userId)

        Log.d("MyPageFragment", "profileList.enqueue 호출전 : ")

        profileList.enqueue(object : Callback<ProfileDto> {
            override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
                Log.d("MyPageFragment", "도착 확인: ")
                val profileDto = response.body()
                Log.d("MyPageFragment", "도착 확인1: profileList $profileDto")
                Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")
                Log.d("MyPageFragment", "도착 확인3: countFromUser ${profileDto?.countFromUser}")
                Log.d("MyPageFragment", "도착 확인4: countToUser ${profileDto?.countToUser}")
                Log.d("MyPageFragment", "도착 확인5: followerDtoList ${profileDto?.followerDtoList}")
                Log.d("MyPageFragment", "도착 확인5: followCheck ${profileDto?.followCheck}")
                if (profileDto != null) {
                    // 팔로워 팔로우수
                    binding.countFromUser.text = profileDto.countFromUser.toString()
                    binding.countToUser.text = profileDto.countToUser.toString()
                    binding.countBoard.text = profileDto.countBoard.toString()
//                    binding.followCheck.text=profileDto.followCheck.toString()

                    // 유저정보
                    binding.userId.text = profileDto.pageUserDto.userid
//                    binding.userName.text = profileDto.pageUserDto.username
//                    binding.userAddress.text = profileDto.pageUserDto.user_address
//                    binding.userPhone.text = profileDto.pageUserDto.userphone
//                    binding.userRole.text = profileDto.pageUserDto.user_role
//                    binding.gender.text = profileDto.pageUserDto.gender


                    // 다른 필요한 데이터들도 마찬가지로 설정

                    Glide.with(requireContext())
                        .load(profileDto.pageUserDto.user_image)
                        .override(900, 900)
                        .into(binding.userImage)

                    Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")

                    ProfileAdapter(this@MyPageFragment, listOf(profileDto.pageUserDto))
                    boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, profileDto.boards.content)

                    binding.boardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.boardRecyclerView.adapter = boardAdapter

                    // 팔로잉
                    binding.following.setOnClickListener {
                        val followList: List<FollowingDto> = profileDto.followingDtoList ?: emptyList()
                        Log.d("MyPageFragment", "도착 확인6: followingDtoList $followList")
                        if (followList.isEmpty()) {
                            ShowMessage("실패", "데이터를 찾을 수 없습니다.")
                            return@setOnClickListener
                        }

                        val buffer = StringBuffer()
                        for (followDto in followList) {
                            buffer.append(
                                // 코틀린 3중 따옴표, 멀티 라인.
                                // FollowDto의 각 속성을 가져와서 문자열로 만듭니다.
                                """
                        ID: ${followDto.id}
                        이름: ${followDto.name}
                        프로필 이미지: ${followDto.profileImage}
                        구독 상태: ${followDto.subscribeState}
                    """.trimIndent()
                            )
                        }

                        ShowMessage("회원목록", buffer.toString())
                    }

                    // 팔로워
                    binding.follower.setOnClickListener {
                        // 팔로워 리스트 가져오기
                        val followerList: List<FollowerDto> = profileDto.followerDtoList
                        Log.d("MyPageFragment", "도착 확인6: followerDtoList $followerList")
                        if (followerList.isEmpty()) {
                            ShowMessage("실패", "데이터를 찾을 수 없습니다.")
                            return@setOnClickListener
                        }

                        val buffer = StringBuffer()
                        for (followingDto in followerList) {
                            buffer.append(
                                // 코틀린 3중 따옴표, 멀티 라인.
                                // FollowDto의 각 속성을 가져와서 문자열로 만듭니다.
                                """
                        ID: ${followingDto.id}
                        이름: ${followingDto.name}
                        프로필 이미지: ${followingDto.profileImage}
                        구독 상태: ${followingDto.subscribeState}
                    """.trimIndent()
                            )
                        }

                        ShowMessage("팔로워 목록", buffer.toString())

                    }
                } else {
                    Log.e("MyPageFragment", "Response body is null.")
                }
            }


            override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("MyPageFragment", " 통신 실패")
            }
        })

        return binding.root
    }

    fun ShowMessage(title: String?, Message: String?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(Message)
        builder.show()
    }
}

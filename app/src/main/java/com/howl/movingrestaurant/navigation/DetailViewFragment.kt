package com.howl.movingrestaurant.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.howl.movingrestaurant.R
import com.howl.movingrestaurant.navigation.model.AlarmDTO
import com.howl.movingrestaurant.navigation.model.ContentDTO
import com.howl.movingrestaurant.navigation.util.FcmPush
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import android.widget.Toast
import com.facebook.FacebookSdk.getApplicationContext
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.facebook.FacebookSdk.getApplicationContext

import com.howl.movingrestaurant.ModifyActivity


class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener
                querySnapshot!!.documents.reverse()
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

            //likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs!![p1].favoriteCount

            //This code is when the button is clicked
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1)
            }
            viewholder.modify.visibility = View.GONE
            viewholder.delete.visibility = View.GONE
            if(contentDTOs!![p1].uid == uid) {
                viewholder.modify.visibility = View.VISIBLE
                viewholder.delete.visibility = View.VISIBLE
                //수정 버튼
                viewholder.modify.setOnClickListener {
                    val intent = Intent(getApplicationContext(), ModifyActivity::class.java)
                    intent.putExtra("ex", contentDTOs!![p1].explain)
                    startActivity(intent)
                }

                //삭제 버튼
                viewholder.delete.setOnClickListener {
                    show(contentDTOs!![p1].explain.toString())
                }
            }
            //This code is when the page is loaded
            if(contentDTOs!![p1].favorites.containsKey(uid)){
                //This is like status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)

            }else{
                //This is unlike status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            //This code is when the profile image is clicked
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[p1].uid)
                bundle.putString("userId",contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[p1])
                intent.putExtra("destinationUid",contentDTOs[p1].uid)
                startActivity(intent)
            }
        }
        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    //When the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }else{
                    //When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }
                transaction.set(tsDoc,contentDTO)
            }





        }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid,"Howlstagram",message)
        }
        fun show(temp : String) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("")
            builder.setMessage(temp + " 글을 삭제하겠습니까")
            builder.setPositiveButton("예",
                DialogInterface.OnClickListener { dialog, which ->
                    Toast.makeText(
                        getApplicationContext(),
                        "예를 선택했습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                })
            builder.setNegativeButton("아니오",
                DialogInterface.OnClickListener { dialog, which ->
                    Toast.makeText(
                        getApplicationContext(),
                        "아니오를 선택했습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                })
            builder.show()
        }

    }
}
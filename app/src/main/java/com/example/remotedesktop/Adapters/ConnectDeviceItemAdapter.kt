package com.example.remotedesktop.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.remotedesktop.Firebase.ConnectedDevice
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.R

class ConnectDeviceItemAdapter(context: Context, private val items: List<User>) :
    ArrayAdapter<User>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.connected_device_item, parent, false)
        val viewHolder: ViewHolder

        if (itemView.tag == null) {
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        val item = items[position]
        viewHolder.bind(item)

        return itemView
    }

    inner class ViewHolder(itemView: View) {
        private val connectedDeviceUserNameTextView: TextView = itemView.findViewById(R.id.connected_device_user_name)

        fun bind(user: User) {
            connectedDeviceUserNameTextView.text = user.name
        }
    }
}
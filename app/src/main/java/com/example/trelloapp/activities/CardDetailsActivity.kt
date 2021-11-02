package com.example.trelloapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloapp.Database.FireStoreClass
import com.example.trelloapp.R
import com.example.trelloapp.adapters.CardMemberListItemsAdapter
import com.example.trelloapp.dialogs.LabelColorListDialog
import com.example.trelloapp.dialogs.MembersListDialog
import com.example.trelloapp.models.*
import com.example.trelloapp.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var mMembersDetailList: ArrayList<User>

    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setUpActionBar()

        et_name_card_details.setText(
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition]
                .labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        btn_update_card_details.setOnClickListener {
            if (et_name_card_details.text.toString().isNotEmpty()) {
                updateCardDetails()
            }
            else {
                Toast.makeText(
                    this,
                    "Please Enter a Card Name",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tv_select_label_color.setOnClickListener { labelColorListDialog() }

        tv_select_due_date.setOnClickListener { memberListDialog() }

        setUpSelectedMembers()

        mSelectedDueDateMilliSeconds =
            mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition]
                .dueDate

        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate =
                simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))

            tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_details_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = mBoardDetails
                                    .taskList[mTaskListPosition]
                                    .cards[mCardPosition]
                                    .name

        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition =
                intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition =
                intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList =
                intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun memberListDialog() {
        var cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.isNotEmpty()) {
            for (member_id in mMembersDetailList.indices) {
                for (cardMember_id in cardAssignedMembersList) {
                    if (mMembersDetailList[member_id].id == cardMember_id) {
                        mMembersDetailList[member_id].selected = true
                    }
                }
            }
        }
        else {
            for (member_id in mMembersDetailList.indices) {
                mMembersDetailList[member_id].selected = false
            }
        }

        val listDialog = object: MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.select_members)
        ){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo.contains(user.id)) {
                        mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo.add(user.id)
                    }
                }
                else {
                    mBoardDetails
                        .taskList[mTaskListPosition]
                        .cards[mCardPosition]
                        .assignedTo.remove(user.id)

                    for (index in mMembersDetailList.indices) {
                        if (mMembersDetailList[index].id == user.id) {
                            mMembersDetailList[index].selected = false
                        }
                    }
                }
                setUpSelectedMembers()
            }
        }
        listDialog.show()
    }

    private fun updateCardDetails() {
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )

        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialog, _->
            dialog.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorListDialog() {
        val colorList: ArrayList<String> = colorsList()
        val listDialog = object: LabelColorListDialog(
            this,
            colorList,
            mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setUpSelectedMembers() {
        val cardAssignedMemberList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        for (memberID in mMembersDetailList.indices) {
            for (cardMemberID in cardAssignedMemberList) {
                if (mMembersDetailList[memberID].id == cardMemberID) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[memberID].id,
                        mMembersDetailList[memberID].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.isNotEmpty()) {
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager =
                GridLayoutManager(
                    this,
                    6
                )

            val adapter = CardMemberListItemsAdapter(
                this,
                selectedMembersList,
                true
            )

            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object: CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        memberListDialog()
                    }
                }
            )
        }
        else {
            tv_select_members.visibility = View.VISIBLE
            rv_members_list.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener {view, year, month, day ->
                    val sDay = if (day < 10) "0$day" else "$day"
                    val sMonth =
                        if ((month + 1) < 10) "0${month + 1}" else "${month+1}"

                    val selectedDate = "$sDay/$sMonth/$year"
                    tv_select_due_date.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val theDate = sdf.parse(selectedDate)
                    mSelectedDueDateMilliSeconds = theDate!!.time
                },
                year,
                month,
                day
            )
        dpd.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(
                    mBoardDetails
                        .taskList[mTaskListPosition]
                        .cards[mCardPosition]
                        .name
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
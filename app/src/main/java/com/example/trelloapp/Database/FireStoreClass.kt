package com.example.trelloapp.Database

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloapp.activities.*
import com.example.trelloapp.models.Board
import com.example.trelloapp.models.Task
import com.example.trelloapp.models.User
import com.example.trelloapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener { activity.userRegistered() }
                .addOnFailureListener { Log.e(activity.javaClass.simpleName, "Error") }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())  //wont override if value already present
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(
                    activity,
                    "Board is created successfully",
                    Toast.LENGTH_LONG
                ).show()

                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating board",
                    exception
                )
            }
    }

    fun updateUserProfileData(
            activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .update(userHashMap)
                .addOnSuccessListener {
                    Log.i(activity.javaClass.simpleName, "Profile Data Updated")
                    Toast.makeText(
                            activity,
                            "Profile Updated Successfully",
                            Toast.LENGTH_LONG
                    ).show()
                    when (activity) {
                        is MainActivity -> {
                            activity.tokenUpdateSuccess()
                        }
                        is MyProfileActivity -> {
                            activity.profileUpdateSuccess()
                        }
                    }
                }.addOnFailureListener {
                    when (activity) {
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MyProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName, "Error while creating board")
                    Toast.makeText(
                            activity,
                            "Profile Updating Error",
                            Toast.LENGTH_LONG
                    ).show()
                }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)!!

                    when (activity) {
                        is SignInActivity -> { activity.signInSuccess(loggedInUser) }
                        is MainActivity -> { activity.updateNavigationUserDetails(loggedInUser, readBoardList) }
                        is MyProfileActivity -> { activity.setUserDataInUI(loggedInUser) }
                    }
                }
                .addOnFailureListener {
                    when (activity) {
                        is SignInActivity -> { activity.hideProgressDialog() }
                        is MainActivity -> { activity.hideProgressDialog() }
                    }
                    Log.e("SignInUser", "Error writing document")
                }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())

                val boardList: ArrayList<Board> = ArrayList()
                for (doc in document.documents) {
                    val board = doc.toObject(Board::class.java)
                    board!!.documentID = doc.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName, "Error while creating boards")
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Task List Updated")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }
                else if(activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                else if(activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating board : $e")
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentID = document.id
                activity.boardDetails(board)
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()
                for (doc in document.documents) {
                    val user = doc.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity) {
                    activity.setUpMembersList(usersList)
                }
                else if (activity is TaskListActivity) {
                    activity.boardMembersDetailsList(usersList)
                }
            }.addOnFailureListener { exception ->
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is TaskListActivity){
                    activity.hideProgressDialog();
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board",
                    exception
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.isNotEmpty()) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }
                else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    exception
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board",
                    exception
                )
            }
    }
}
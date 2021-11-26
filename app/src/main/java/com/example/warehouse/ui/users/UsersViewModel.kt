package com.example.warehouse.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warehouse.arch.Event
import com.example.warehouse.database.WarehouseDatabase
import com.example.warehouse.database.entity.ItemEntity
import com.example.warehouse.database.entity.UserEntity
import com.example.warehouse.database.entity.UserWithOrderEntity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private lateinit var repository: UsersRepository

    val userWithOrderEntityLiveData = MutableLiveData<List<UserWithOrderEntity>>()

    val transactionCompleteLiveData = MutableLiveData<Event<Boolean>>()

    fun init (warehouseDatabase: WarehouseDatabase) {
        repository = UsersRepository(warehouseDatabase)

        viewModelScope.launch {
            repository.getAllUserWithOrderEntity().collect { users ->
                userWithOrderEntityLiveData.postValue(users)

                updateUsersViewState(users)
            }
        }

    }

    private val _usersViewStateLiveData = MutableLiveData<UsersViewState>()
    val usersViewStateLiveData: LiveData<UsersViewState>
        get() = _usersViewStateLiveData

    data class UsersViewState(
        val dataList: List<DataItem<*>> = emptyList(),
        val isLoading: Boolean = false
    ) {
        data class DataItem<T> (
            val data: T
        )
    }

    private fun updateUsersViewState(items: List<UserWithOrderEntity>) {
        val dataList = ArrayList<UsersViewState.DataItem<*>>()

        items.forEach { item ->
            dataList.add(UsersViewState.DataItem(item))
        }

        _usersViewStateLiveData.postValue(UsersViewState(dataList = dataList, isLoading = false))
    }

    fun insertUser(userEntity: UserEntity) {
        viewModelScope.launch {
            repository.insertUser(userEntity)

            transactionCompleteLiveData.postValue(Event(true))
        }
    }

    fun deleteUser(userEntity: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(userEntity)
        }
    }

    fun updateUser(userEntity: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(userEntity)

            transactionCompleteLiveData.postValue(Event(true))
        }
    }
}
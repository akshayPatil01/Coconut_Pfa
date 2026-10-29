package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.CategoryDao
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbCategoryKind
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    fun getCategoriesByKind(kind: DbCategoryKind): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByKindFlow(kind)
    suspend fun getCategoryById(id: String): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun saveCategory(category: CategoryEntity) = categoryDao.insert(category)
    suspend fun saveAllCategories(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)
}

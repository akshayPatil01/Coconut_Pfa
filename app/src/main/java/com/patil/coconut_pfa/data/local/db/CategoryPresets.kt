package com.patil.coconut_pfa.data.local.db

import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbCategoryKind

object CategoryPresets {
    val presets: List<CategoryEntity> by lazy {
        val list = mutableListOf<CategoryEntity>()
        var sortOrder = 1

        // 1. Food & dining
        val foodParentId = "cat_exp_food_dining"
        list.add(
            CategoryEntity(
                id = foodParentId,
                name = "Food & dining",
                parentId = null,
                kind = DbCategoryKind.EXPENSE,
                icon = "restaurant",
                colour = 0xFFFF5722L,
                isPreset = true,
                archived = false,
                sortOrder = sortOrder++
            )
        )
        list.add(CategoryEntity(id = "cat_exp_restaurants", name = "Restaurants", parentId = foodParentId, kind = DbCategoryKind.EXPENSE, icon = "restaurant_menu", colour = 0xFFFF5722L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_food_delivery", name = "Food delivery", parentId = foodParentId, kind = DbCategoryKind.EXPENSE, icon = "delivery_dining", colour = 0xFFFF5722L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_tea_coffee_snacks", name = "Tea/coffee & snacks", parentId = foodParentId, kind = DbCategoryKind.EXPENSE, icon = "local_cafe", colour = 0xFFFF5722L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 2. Groceries
        list.add(CategoryEntity(id = "cat_exp_groceries", name = "Groceries", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "local_grocery_store", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 3. Fuel
        list.add(CategoryEntity(id = "cat_exp_fuel", name = "Fuel", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "local_gas_station", colour = 0xFFFF9800L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 4. Transport
        val transportParentId = "cat_exp_transport"
        list.add(CategoryEntity(id = transportParentId, name = "Transport", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "directions_bus", colour = 0xFF2196F3L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_cab_auto", name = "Cab/auto", parentId = transportParentId, kind = DbCategoryKind.EXPENSE, icon = "local_taxi", colour = 0xFF2196F3L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_metro_bus_train", name = "Metro/bus/train", parentId = transportParentId, kind = DbCategoryKind.EXPENSE, icon = "subway", colour = 0xFF2196F3L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_parking_fastag", name = "Parking & FASTag", parentId = transportParentId, kind = DbCategoryKind.EXPENSE, icon = "local_parking", colour = 0xFF2196F3L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 5. Rent
        list.add(CategoryEntity(id = "cat_exp_rent", name = "Rent", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "home", colour = 0xFF9C27B0L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 6. Home & maintenance
        list.add(CategoryEntity(id = "cat_exp_home_maintenance", name = "Home & maintenance", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "build", colour = 0xFF795548L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 7. Bills & recharge
        val billsParentId = "cat_exp_bills_recharge"
        list.add(CategoryEntity(id = billsParentId, name = "Bills & recharge", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "receipt_long", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_electricity", name = "Electricity", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "electric_bolt", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_mobile", name = "Mobile", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "smartphone", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_broadband", name = "Broadband", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "router", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_dth", name = "DTH", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "tv", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_gas", name = "Gas", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "propane_tank", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))
        list.add(CategoryEntity(id = "cat_exp_water", name = "Water", parentId = billsParentId, kind = DbCategoryKind.EXPENSE, icon = "water_drop", colour = 0xFF00BCD4L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 8. Loan interest
        list.add(CategoryEntity(id = "cat_exp_loan_interest", name = "Loan interest", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "percent", colour = 0xFFE91E63L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 9. Shopping
        list.add(CategoryEntity(id = "cat_exp_shopping", name = "Shopping", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "shopping_bag", colour = 0xFFE91E63L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 10. Medical & health
        list.add(CategoryEntity(id = "cat_exp_medical_health", name = "Medical & health", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "medical_services", colour = 0xFFF44336L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 11. Insurance
        list.add(CategoryEntity(id = "cat_exp_insurance", name = "Insurance", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "security", colour = 0xFF3F51B5L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 12. Education
        list.add(CategoryEntity(id = "cat_exp_education", name = "Education", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "school", colour = 0xFF3F51B5L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 13. Entertainment & OTT
        list.add(CategoryEntity(id = "cat_exp_entertainment_ott", name = "Entertainment & OTT", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "movie", colour = 0xFF9C27B0L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 14. Travel
        list.add(CategoryEntity(id = "cat_exp_travel", name = "Travel", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "flight", colour = 0xFF009688L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 15. Personal care
        list.add(CategoryEntity(id = "cat_exp_personal_care", name = "Personal care", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "face", colour = 0xFFE91E63L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 16. Gifts & donations
        list.add(CategoryEntity(id = "cat_exp_gifts_donations", name = "Gifts & donations", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "card_giftcard", colour = 0xFFFF5722L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 17. Family support
        list.add(CategoryEntity(id = "cat_exp_family_support", name = "Family support", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "diversity_3", colour = 0xFF673AB7L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 18. Household help
        list.add(CategoryEntity(id = "cat_exp_household_help", name = "Household help", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "cleaning_services", colour = 0xFF795548L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 19. Fees & charges
        list.add(CategoryEntity(id = "cat_exp_fees_charges", name = "Fees & charges", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "payments", colour = 0xFF607D8BL, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 20. Taxes
        list.add(CategoryEntity(id = "cat_exp_taxes", name = "Taxes", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "account_balance", colour = 0xFF607D8BL, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 21. Miscellaneous
        list.add(CategoryEntity(id = "cat_exp_miscellaneous", name = "Miscellaneous", parentId = null, kind = DbCategoryKind.EXPENSE, icon = "category", colour = 0xFF9E9E9EL, isPreset = true, archived = false, sortOrder = sortOrder++))

        // --- INCOME CATEGORIES ---
        // 1. Salary
        list.add(CategoryEntity(id = "cat_inc_salary", name = "Salary", parentId = null, kind = DbCategoryKind.INCOME, icon = "payments", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 2. Interest
        list.add(CategoryEntity(id = "cat_inc_interest", name = "Interest", parentId = null, kind = DbCategoryKind.INCOME, icon = "account_balance", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 3. Cashback & rewards
        list.add(CategoryEntity(id = "cat_inc_cashback_rewards", name = "Cashback & rewards", parentId = null, kind = DbCategoryKind.INCOME, icon = "redeem", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 4. Dividends
        list.add(CategoryEntity(id = "cat_inc_dividends", name = "Dividends", parentId = null, kind = DbCategoryKind.INCOME, icon = "trending_up", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 5. Gifts received
        list.add(CategoryEntity(id = "cat_inc_gifts_received", name = "Gifts received", parentId = null, kind = DbCategoryKind.INCOME, icon = "card_giftcard", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        // 6. Other income
        list.add(CategoryEntity(id = "cat_inc_other_income", name = "Other income", parentId = null, kind = DbCategoryKind.INCOME, icon = "attach_money", colour = 0xFF4CAF50L, isPreset = true, archived = false, sortOrder = sortOrder++))

        list
    }
}

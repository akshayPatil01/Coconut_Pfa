package com.patil.coconut_pfa.domain.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserTest {

    private val now = System.currentTimeMillis()

    // ----------------------------------------------------
    // 1. SENDER FILTER TESTS
    // ----------------------------------------------------

    @Test
    fun `non bank non transaction senders return null`() {
        val nonBankSenders = listOf("AMAZON", "SWIGGY", "ZOMATO", "INFO-SMS", "FLIPKT")
        val sampleBody = "Your order has been shipped."

        for (sender in nonBankSenders) {
            val result = SmsParser.parse(sender, sampleBody, now)
            assertNull("Expected null for non-bank non-txn sender: $sender", result)
        }
    }

    @Test
    fun `valid bank senders proceed to parse`() {
        val result = SmsParser.parse("AX-HDFCBK", "Rs. 500.00 debited from A/C XX4321 at Coffee Shop on 10-Nov. Avl Bal: Rs. 10,000.00", now)
        assertNotNull(result)
        assertEquals("HDFC Bank", result?.institution)
    }

    @Test
    fun `trai circle suffix senders parse correctly`() {
        val hdfcSuffixResult = SmsParser.parse("AX-HDFCBK-S", "Rs. 500.00 debited from A/C XX4321 at Coffee Shop. Avl Bal: Rs. 10,000.00", now)
        assertNotNull(hdfcSuffixResult)
        assertEquals("HDFC Bank", hdfcSuffixResult?.institution)

        val iciciSuffixResult = SmsParser.parse("AD-ICICIB-A", "INR 850.00 spent on ICICI Bank Card XX4321 at ZOMATO.", now)
        assertNotNull(iciciSuffixResult)
        assertEquals("ICICI Bank", iciciSuffixResult?.institution)

        val sbiSuffixResult = SmsParser.parse("JM-SBIINB-T", "Your A/C XXXXX123456 debited by Rs 1,200.00. A/c Bal: Rs 8,500.00.", now)
        assertNotNull(sbiSuffixResult)
        assertEquals("SBI", sbiSuffixResult?.institution)
    }

    @Test
    fun `fallback to bank institution for generic transaction sender`() {
        val result = SmsParser.parse("VK-NOTIFY", "Rs. 2,500.00 debited from A/C XX3344 at BookMyShow. Avl Bal: Rs 18,000.00.", now)
        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(250000L, result?.amountPaise)
        assertEquals("3344", result?.last4)
        assertEquals("BookMyShow", result?.merchant)
    }

    // ----------------------------------------------------
    // 2. IGNORE TESTS (OTP, Promo, Failed/Declined)
    // ----------------------------------------------------

    @Test
    fun `otp message returns IGNORE`() {
        val body = "123456 is your OTP for transaction of Rs 1,500.00 at Amazon on HDFC Bank Card XX9876. Do not share code."
        val result = SmsParser.parse("AX-HDFCBK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.IGNORE, result?.type)
        assertEquals("HDFC Bank", result?.institution)
    }

    @Test
    fun `promotional message returns IGNORE`() {
        val body = "Congratulations! You are eligible for a pre-approved personal loan of Rs 5,00,000. Apply now at ICICI Bank."
        val result = SmsParser.parse("AD-ICICIB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.IGNORE, result?.type)
        assertEquals("ICICI Bank", result?.institution)
    }

    @Test
    fun `failed or declined transaction returns IGNORE`() {
        val body = "Txn of Rs 2,000.00 on SBI Card XX8812 was DECLINED due to insufficient balance."
        val result = SmsParser.parse("SBIINB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.IGNORE, result?.type)
        assertEquals("SBI", result?.institution)
    }

    // ----------------------------------------------------
    // 3. HDFC BANK TESTS
    // ----------------------------------------------------

    @Test
    fun `hdfc account debit parsed correctly`() {
        val body = "Rs. 1,450.00 debited from A/C XX4321 at COFFEE SHOP on 12-Nov-23. Avl Bal: Rs. 2,45,000.00. Ref 33129012."
        val result = SmsParser.parse("AX-HDFCBK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(145000L, result?.amountPaise)
        assertEquals("4321", result?.last4)
        assertEquals("COFFEE SHOP", result?.merchant)
        assertEquals(24500000L, result?.availableBalancePaise)
        assertEquals("33129012", result?.refNo)
        assertEquals("HDFC Bank", result?.institution)
    }

    @Test
    fun `hdfc account credit parsed correctly`() {
        val body = "Rs. 25,000.00 credited to HDFC Bank A/C XX4321 on 01-Nov-23 by NEFT-SALARY CORP. Avl Bal: Rs. 1,50,000.00. Ref 99887766."
        val result = SmsParser.parse("AX-HDFCBK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CREDIT, result?.type)
        assertEquals(2500000L, result?.amountPaise)
        assertEquals("4321", result?.last4)
        assertEquals("NEFT-SALARY CORP", result?.merchant)
        assertEquals(15000000L, result?.availableBalancePaise)
        assertEquals("99887766", result?.refNo)
        assertEquals("HDFC Bank", result?.institution)
    }

    @Test
    fun `hdfc card spend parsed correctly`() {
        val body = "Alert: You've spent Rs. 3,299.00 on HDFC Bank Credit Card XX9876 at AMAZON INDIA on 05-Nov-23."
        val result = SmsParser.parse("VM-HDFCCC", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_SPEND, result?.type)
        assertEquals(329900L, result?.amountPaise)
        assertEquals("9876", result?.last4)
        assertEquals("AMAZON INDIA", result?.merchant)
        assertEquals("HDFC Bank", result?.institution)
    }

    @Test
    fun `hdfc card statement parsed correctly`() {
        val body = "Total Amount Due on HDFC Bank Card XX9876 for Oct 23 is Rs. 18,450.00. Min Amount Due: Rs. 920.00. Due Date: 20-Nov-2023."
        val result = SmsParser.parse("VM-HDFCCC", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_STATEMENT, result?.type)
        assertEquals("9876", result?.last4)
        assertEquals(1845000L, result?.statementTotalPaise)
        assertEquals(92000L, result?.minDuePaise)
        assertEquals("20-Nov-2023", result?.dueDate)
        assertEquals("HDFC Bank", result?.institution)
    }

    // ----------------------------------------------------
    // 4. ICICI BANK TESTS
    // ----------------------------------------------------

    @Test
    fun `icici account debit parsed correctly`() {
        val body = "ICICI Bank A/C XX5678 debited for Rs 2,100.00 on 10-Nov-23. Info: D-MART. Avl Bal: Rs 45,200.00. Ref No 11223344."
        val result = SmsParser.parse("BZ-ICICIB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(210000L, result?.amountPaise)
        assertEquals("5678", result?.last4)
        assertEquals("D-MART", result?.merchant)
        assertEquals(4520000L, result?.availableBalancePaise)
        assertEquals("11223344", result?.refNo)
        assertEquals("ICICI Bank", result?.institution)
    }

    @Test
    fun `icici account credit parsed correctly`() {
        val body = "Dear Customer, ICICI Bank A/C XX5678 is credited with Rs 10,000.00 on 02-Nov-23 by UPI/30129012/RAHUL. Avl Bal: Rs 55,200.00."
        val result = SmsParser.parse("BZ-ICICIB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CREDIT, result?.type)
        assertEquals(1000000L, result?.amountPaise)
        assertEquals("5678", result?.last4)
        assertEquals("UPI/30129012/RAHUL", result?.merchant)
        assertEquals(5520000L, result?.availableBalancePaise)
        assertEquals("30129012", result?.refNo)
        assertEquals("ICICI Bank", result?.institution)
    }

    @Test
    fun `icici card spend parsed correctly`() {
        val body = "INR 850.00 spent on ICICI Bank Card XX4321 at ZOMATO on 08-Nov-23."
        val result = SmsParser.parse("VM-ICICIC", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_SPEND, result?.type)
        assertEquals(85000L, result?.amountPaise)
        assertEquals("4321", result?.last4)
        assertEquals("ZOMATO", result?.merchant)
        assertEquals("ICICI Bank", result?.institution)
    }

    @Test
    fun `icici card statement parsed correctly`() {
        val body = "Statement for ICICI Bank Card XX4321: Total Due: Rs 12,300.00, Min Due: Rs 1,000.00, Due Date: 15-Nov-2023."
        val result = SmsParser.parse("VM-ICICIC", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_STATEMENT, result?.type)
        assertEquals("4321", result?.last4)
        assertEquals(1230000L, result?.statementTotalPaise)
        assertEquals(100000L, result?.minDuePaise)
        assertEquals("15-Nov-2023", result?.dueDate)
        assertEquals("ICICI Bank", result?.institution)
    }

    // ----------------------------------------------------
    // 5. SBI TESTS
    // ----------------------------------------------------

    @Test
    fun `sbi account debit parsed correctly`() {
        val body = "Your A/C XXXXX123456 debited by Rs 1,200.00 on 04Nov23 transfer to UBER. A/c Bal: Rs 8,500.00. Ref 44556677."
        val result = SmsParser.parse("AD-SBIINB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(120000L, result?.amountPaise)
        assertEquals("3456", result?.last4)
        assertEquals("UBER", result?.merchant)
        assertEquals(850000L, result?.availableBalancePaise)
        assertEquals("44556677", result?.refNo)
        assertEquals("SBI", result?.institution)
    }

    @Test
    fun `sbi account credit parsed correctly`() {
        val body = "Your A/C XXXXX123456 credited by Rs 5,000.00 on 03Nov23 by UPI/33441122/REF. A/c Bal: Rs 13,500.00."
        val result = SmsParser.parse("JM-SBIBNK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CREDIT, result?.type)
        assertEquals(500000L, result?.amountPaise)
        assertEquals("3456", result?.last4)
        assertEquals("UPI/33441122/REF", result?.merchant)
        assertEquals(1350000L, result?.availableBalancePaise)
        assertEquals("33441122", result?.refNo)
        assertEquals("SBI", result?.institution)
    }

    @Test
    fun `sbi card spend parsed correctly`() {
        val body = "Rs. 4,500.00 spent on SBI Credit Card ending 8812 at RELIANCE DIGITAL on 09 Nov 23."
        val result = SmsParser.parse("SBICRD", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_SPEND, result?.type)
        assertEquals(450000L, result?.amountPaise)
        assertEquals("8812", result?.last4)
        assertEquals("RELIANCE DIGITAL", result?.merchant)
        assertEquals("SBI", result?.institution)
    }

    @Test
    fun `sbi card statement parsed correctly`() {
        val body = "SBI Card XX8812 Statement: Total Amt Due: Rs 25,600.00, Min Amt Due: Rs 1,280.00. Payment Due Date: 28-Nov-2023."
        val result = SmsParser.parse("SBICRD", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_STATEMENT, result?.type)
        assertEquals("8812", result?.last4)
        assertEquals(2560000L, result?.statementTotalPaise)
        assertEquals(128000L, result?.minDuePaise)
        assertEquals("28-Nov-2023", result?.dueDate)
        assertEquals("SBI", result?.institution)
    }

    // ----------------------------------------------------
    // 6. AXIS BANK TESTS
    // ----------------------------------------------------

    @Test
    fun `axis account debit parsed correctly`() {
        val body = "Rs 3,400.00 debited from Axis Bank A/C XX7890 at SHELL PETROL on 11-11-23. Avail Bal Rs 62,000.00. Ref 88776655."
        val result = SmsParser.parse("AXISBK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(340000L, result?.amountPaise)
        assertEquals("7890", result?.last4)
        assertEquals("SHELL PETROL", result?.merchant)
        assertEquals(6200000L, result?.availableBalancePaise)
        assertEquals("88776655", result?.refNo)
        assertEquals("Axis Bank", result?.institution)
    }

    @Test
    fun `axis account credit parsed correctly`() {
        val body = "Rs 15,000.00 credited to Axis Bank A/C XX7890 on 05-11-23. Info: SALARY. Avail Bal Rs 77,000.00."
        val result = SmsParser.parse("AXISBK", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CREDIT, result?.type)
        assertEquals(1500000L, result?.amountPaise)
        assertEquals("7890", result?.last4)
        assertEquals("SALARY", result?.merchant)
        assertEquals(7700000L, result?.availableBalancePaise)
        assertEquals("Axis Bank", result?.institution)
    }

    @Test
    fun `axis card spend parsed correctly`() {
        val body = "INR 1,800.00 spent on Axis Bank Card XX6543 at MAKE MY TRIP on 07-11-23."
        val result = SmsParser.parse("AXISCB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_SPEND, result?.type)
        assertEquals(180000L, result?.amountPaise)
        assertEquals("6543", result?.last4)
        assertEquals("MAKE MY TRIP", result?.merchant)
        assertEquals("Axis Bank", result?.institution)
    }

    @Test
    fun `axis card statement parsed correctly`() {
        val body = "Axis Bank Credit Card XX6543 Statement: Total Due Rs 14,200.00, Min Due Rs 1,000.00. Due Date 22-Nov-2023."
        val result = SmsParser.parse("AXISCB", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.CARD_STATEMENT, result?.type)
        assertEquals("6543", result?.last4)
        assertEquals(1420000L, result?.statementTotalPaise)
        assertEquals(100000L, result?.minDuePaise)
        assertEquals("22-Nov-2023", result?.dueDate)
        assertEquals("Axis Bank", result?.institution)
    }

    // ----------------------------------------------------
    // 7. GENERIC FALLBACK TESTS
    // ----------------------------------------------------

    @Test
    fun `generic bank debit parsed correctly`() {
        val body = "Rs. 2,500.00 debited from A/C XX3344 at BookMyShow on 14-Nov. Avl Bal: Rs 18,000.00."
        val result = SmsParser.parse("AM-INDUSI", body, now)

        assertNotNull(result)
        assertEquals(ParsedSmsType.DEBIT, result?.type)
        assertEquals(250000L, result?.amountPaise)
        assertEquals("3344", result?.last4)
        assertEquals("BookMyShow", result?.merchant)
        assertEquals(1800000L, result?.availableBalancePaise)
        assertEquals("IndusInd Bank", result?.institution)
    }

    // ----------------------------------------------------
    // 8. AMOUNT PARSING EDGE CASES
    // ----------------------------------------------------

    @Test
    fun `amount parsing with different formats`() {
        assertEquals(12345678L, SmsParserUtils.parseAmountToPaise("1,23,456.78"))
        assertEquals(50000L, SmsParserUtils.parseAmountToPaise("500"))
        assertEquals(1250000L, SmsParserUtils.parseAmountToPaise("12,500.00"))
        assertEquals(25000L, SmsParserUtils.parseAmountToPaise("250"))
        assertEquals(50L, SmsParserUtils.parseAmountToPaise("0.50"))
        assertEquals(120050L, SmsParserUtils.parseAmountToPaise("1,200.5"))
    }

    @Test
    fun `extract amount supports leading and trailing currency`() {
        assertEquals(50000L, SmsParserUtils.extractAmount("Rs. 500 debited"))
        assertEquals(120000L, SmsParserUtils.extractAmount("1,200.00 INR debited"))
        assertEquals(35000L, SmsParserUtils.extractAmount("350 Rs paid to Merchant"))
    }
}

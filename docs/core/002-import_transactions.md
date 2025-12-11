<conversation_summary>
<decisions>

1. **Platform:** The MVP will be a web-only application.
2. **Import Scope:** The MVP will only support CSV file imports from mBank.
3. **Authentication:** A full authentication process (registration, login, password reset) will be implemented using a
   third-party provider (Supabase).
4. **Categorization Engine:** An LLM (e.g., Gemini) called via API will be used.
5. **LLM Input Data:** Only the "Operation Description" field from the CSV will be sent to the LLM.
6. **Categories:** 9 categories are defined: 8 for expenses ("Car", "Events/Parties", "Food", "Meds & Doctors", "
   Bills/Fees", "Clothing", "Travel", "Other") and 1 for income ("Income"). The list will be hardcoded in the backend
   and exposed via API.
7. **Income Handling:** Transactions with a positive amount will be automatically categorized as "Income" and will *not*
   be sent to the LLM.
8. **Import Process (UX):** The import will be a synchronous (UI-blocking) process. The user will see a loading screen
   with a message that the process may take a few minutes.
9. **Verification Screen:** The user must manually fill in *all* empty categories for expenses before saving. Categories
   for income will be pre-filled and locked.
10. **LLM Error Handling:** If the LLM fails to return a valid category (API error, bad response), the category field
    will remain empty and require manual input from the user.
11. **Deduplication:** The system will prevent duplicates by checking a composite key of
    `Date + Amount + Operation Description` before saving to the database.
12. **Informing about Duplicates:** After import, the user will receive a message (e.g., "Imported 15 new transactions.
    85 duplicates were skipped.").
13. **Data Storage:** The application *will* store the "Operation Description" in the database after import. The
    original CSV file will *not* be stored.
14. **Main Table:** It will display a list of transactions (Date, Description, Category, Amount) without additional
    analytics.
15. **Table Features:** The user will be able to edit (category only) and delete (with a confirmation modal) individual
    transactions.
16. **Empty State:** Upon first login, the user will see the headers of an empty table and an "Import" button.
17. **Success Measurement:** This will be implemented by adding a boolean field `category_changed_manually` to the
    database and measured by a manual SQL query in the Supabase panel.

</decisions>

<matched_recommendations>

1. **Authentication:** Decided to use a third-party provider (Supabase) to quickly implement a complete and secure
   authentication system (registration, login, password reset).
2. **Import UX:** Accepted the recommendation for a synchronous (blocking) loading screen during LLM data processing to
   simplify the MVP architecture.
3. **Import Validation:** Established that saving the import will be impossible until the user manually fills in all
   transactions (expenses) that the LLM did not assign a category to.
4. **Income Handling:** Decided to separate the logic for income – it will be automatically categorized, excluded from
   LLM processing, and locked for editing on the verification screen.
5. **Deduplication:** Accepted the deduplication mechanism based on a business key (
   `Date + Amount + Operation Description`) to avoid re-importing the same transactions.
6. **Table Management:** Accepted recommendations regarding limiting editing in the main table (category only) and the
   need for a confirmation modal for deletion.
7. **Success Measurement:** Accepted the technical proposal for measuring the success criterion (adding a
   `category_changed_manually` flag in the database).
8. **Database Structure:** Approved the proposed `transactions` table structure, including a `transaction_type` field
   and the deduplication key.
   </matched_recommendations>

<prd_planning_summary>

### a. Main Functional Requirements (MVP)

1. **User Authentication:**
    * User can create a new account (Registration).
    * User can log into an existing account (Login).
    * User can reset their password (Password Reset).
    * (Requirement met by integration with Supabase Auth).
2. **Transaction Import:**
    * User can upload a CSV file compliant with the mBank standard.
    * System must validate the file (extension client-side, structure server-side).
    * System must inform about file validation errors (e.g., "Incorrect file format").
3. **Processing and Categorization (Backend):**
    * System must distinguish between expenses (negative amount) and income (positive amount).
    * System must check each transaction for duplicates (based on the `Date+Amount+Description` key).
    * For expenses: System sends the "Operation Description" to the LLM API (Gemini) along with the list of 8 allowed
      categories.
    * For income: System automatically assigns the "Income" category.
4. **Import Verification Screen:**
    * System must display a list of *only* new transactions.
    * System must display a message about the number of imported and skipped (duplicated) transactions.
    * The list must show Date, Description, Amount, and a Category selection field.
    * Categories returned by the LLM must be pre-selected.
    * Category fields for income must be pre-selected ("Income") and locked (disabled) for editing.
    * System must disable the "Save" button until all categories for expenses are filled in.
5. **Main Transaction Table:**
    * System must display all saved transactions (Date, Description, Category, Amount) sorted by date in descending
      order by default.
    * In the empty state (before the first import), the system must show the table headers and an "Import" button.
    * User must be able to edit the category for each transaction.
    * User must be able to delete a transaction (with a confirmation modal).

### b. Key User Stories and Flows

* **Flow 1: First Use (Registration & Import)**

    1. As a new user, I want to create an account so I can use the application.
    2. As a logged-in user (for the first time), I see an empty screen and a clear call to action to import a file.
    3. I want to select an mBank CSV file and upload it.
    4. I want to see a loading screen, informing me that the system is processing the data (LLM categorization).
    5. I want to see a list of my transactions with automatically suggested categories.
    6. I want to manually correct categories that the AI assigned incorrectly or left empty.
    7. I want to save the verified transactions to my account.
    8. After saving, I want to be taken to the main table, where I will see my saved transactions.

* **Flow 2: Managing Transactions (Edit/Delete)**

    1. As a logged-in user, I want to see a list of all my historical transactions.
    2. I want to find a transaction whose category was saved incorrectly and change its category.
    3. I want to find a transaction that was imported by mistake and permanently delete it, after confirming my action.

* **Flow 3: Subsequent Import (Handling Duplicates)**

    1. As a returning user, I want to import a new CSV file, which may contain some transactions I have already
       imported.
    2. The system must automatically recognize and skip duplicated transactions.
    3. I want to see *only* the new transactions on the verification screen and a message about how many duplicates were
       skipped.

### c. Important Success Criteria and Measurement

* **Success Criterion (Main):** At least 50% of categories automatically generated by the LLM are accepted by users.
* **Definition of "Acceptance":** The user did not manually change the category suggested by the LLM on the import
  verification screen.
* **Measurement Method (Technical):** The `transactions` table contains a boolean field `category_changed_manually`.
    * If LLM suggested a category and the user *did not* change it -> `false`.
    * If LLM suggested a category and the user changed it -> `true`.
    * If LLM did not suggest a category (empty field) and the user filled it in -> `null` or `false` (does not count
      towards the metric as an "error", but as "no suggestion").
* **Measurement Method (Business):** A manual SQL query run by the team in the Supabase panel, calculating the ratio of
  `false` to `true` for transactions where an LLM suggestion was present.
  </prd_planning_summary>

<unresolved_issues>
Based on the discussion, all key strategic and technical issues for the MVP have been resolved. No unresolved issues
blocking the start of work on the detailed PRD were identified.
</unresolved_issues>
</conversation_summary>
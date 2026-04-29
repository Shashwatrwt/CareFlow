# Supabase Database Setup Guide for CareFlow

## Connection Details

✅ **Status:** Connected
- **Project URL:** https://ckkugkqmjyxpsibhaqis.supabase.co
- **Database:** postgres
- **User:** postgres
- **Port:** 5432
- **SSL Mode:** require (enabled)

---

## Step 1: Access Supabase SQL Editor

1. Go to: https://app.supabase.com/
2. Click on your project: **CareFlow**
3. Go to **SQL Editor** (left sidebar)
4. Click **New Query** or **Create New Query**

---

## Step 2: Run SQL Scripts to Create Tables

Copy and paste each SQL script below into Supabase SQL Editor and run them in order:

### Script 1: Create PATIENTS Table

```sql
CREATE TABLE IF NOT EXISTS patients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    age INTEGER NOT NULL CHECK (age > 0),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'ADMITTED', 'DISCHARGED')),
    bed_id INTEGER DEFAULT -1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patients_status ON patients(status);
CREATE INDEX idx_patients_severity ON patients(severity);
```

### Script 2: Create BEDS Table

```sql
CREATE TABLE IF NOT EXISTS beds (
    bed_id SERIAL PRIMARY KEY,
    is_occupied BOOLEAN DEFAULT FALSE,
    patient_id INTEGER DEFAULT -1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert 50 beds (adjust number as needed)
INSERT INTO beds (is_occupied, patient_id) 
VALUES 
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1),
    (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1), (FALSE, -1);

CREATE INDEX idx_beds_occupied ON beds(is_occupied);
```

### Script 3: Create PATIENT_HISTORY Table

```sql
DROP TABLE IF EXISTS patient_history CASCADE;

CREATE TABLE IF NOT EXISTS patient_history (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    discharged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stay_duration INTEGER DEFAULT 24,
    final_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patient_history_discharged ON patient_history(discharged_at);
```

---

## Step 3: Verify Tables in Supabase

1. Go to **Table Editor** (left sidebar)
2. You should see three new tables:
   - `patients`
   - `beds`
   - `patient_history`

3. Click each table to verify it has the correct columns and sample data

---

## Step 4: Run the CareFlow Application

```powershell
cd C:\Users\Mohit\Downloads\m\CareFlow
.\apache-maven-3.9.6\bin\mvn.cmd compile exec:java "-Dexec.mainClass=ui.MainApp"
```

The application will:
- ✅ Connect to your Supabase database
- ✅ Load existing patients from the database
- ✅ Show hospital statistics
- ✅ Allow you to register new patients
- ✅ Manage bed allocations
- ✅ Track discharge history

---

## Troubleshooting

### ❌ Connection Error: "Authentication failed"
**Solution:** 
- Check username and password are correct
- Verify you're using the correct Supabase project
- Reset database password in Supabase settings if needed

### ❌ Table not found error
**Solution:** 
- Run the SQL scripts above in Supabase SQL Editor
- Verify tables appear in "Table Editor"

### ❌ SSL/Connection timeout
**Solution:** 
- Ensure your internet connection is stable
- Check if Supabase project is active
- Verify firewall isn't blocking connections to Supabase

### ❌ Can't see new records in Supabase
**Solution:** 
- Supabase syncs in real-time, but may have slight delay
- Refresh the Table Editor view
- Check the `updated_at` timestamp to confirm record was inserted

---

## Database Schema Overview

### patients
- Stores all patient information
- Tracks current status (WAITING, ADMITTED, DISCHARGED)
- Linked to beds via bed_id

### beds
- Tracks all hospital beds (default: 50)
- Records occupancy status
- Tracks which patient occupies each bed

### patient_history
- Archives discharged patients
- Maintains historical record with discharge timestamp
- Used for generating discharge reports

---

## API Key Security

⚠️ **IMPORTANT:** Your Supabase credentials are stored in `DBConnection.java`. 

**For production, consider:**
1. Moving credentials to `application.properties` file
2. Using environment variables
3. Using Supabase Row Level Security (RLS) policies
4. Creating a separate read-only API key for client access

See [Supabase Security Best Practices](https://supabase.com/docs/guides/auth)

---

## Next Steps

1. ✅ Run SQL scripts to create tables
2. ✅ Verify tables in Supabase
3. ✅ Launch the CareFlow application
4. ✅ Test registering patients and allocating beds
5. ✅ Verify data appears in Supabase Table Editor in real-time

**Questions?** Check the Supabase documentation: https://supabase.com/docs

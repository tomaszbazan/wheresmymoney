# Supabase Configuration Setup

This document explains how to configure Supabase credentials for the Where's My Money Flutter app.

## Development Setup

### 1. Create Local Configuration File

Copy the example configuration file:

```bash
cp assets/config/supabase.json.example assets/config/supabase.json
```

### 2. Edit Configuration

Update `assets/config/supabase.json` with your actual Supabase credentials:

```json
{
  "url": "https://your-actual-project-ref.supabase.co",
  "anonKey": "your-actual-supabase-anon-key"
}
```

**⚠️ Important:** Never commit the actual `supabase.json` file to Git. It's already in `.gitignore`.

### 3. Get Supabase Credentials

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Select your project
3. Go to Settings > API
4. Copy the following:
   - **Project URL** → use as `url`
   - **Project API keys** → **anon/public** key → use as `anonKey`

## Production/CI Setup (GitHub Actions)

### 1. Set GitHub Secrets

In your GitHub repository:

1. Go to Settings > Secrets and variables > Actions
2. Add the following repository secrets:
   - `SUPABASE_URL`: Your Supabase project URL
   - `SUPABASE_ANON_KEY`: Your Supabase anon/public key

### 2. Manual Configuration Generation

You can generate the config manually in any environment:

```bash
# Set environment variables
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_ANON_KEY="your-anon-key"

# Generate configuration
./scripts/generate_config.sh
```

The script will create `assets/config/supabase.json` with your credentials.

## Configuration Loading Priority

The app loads configuration in the following order:

1. **Environment Variables** (highest priority)
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`

2. **Assets Configuration File**
   - `assets/config/supabase.json`

3. **Fallback Configuration** (lowest priority)
   - Default placeholder values for development

## Troubleshooting

### "Could not load Supabase config" Error

This usually means:
1. You haven't created the `assets/config/supabase.json` file
2. The file has invalid JSON syntax
3. Environment variables are not set in production

### Development Fallback

If configuration fails to load, the app will use fallback values:
- URL: `https://your-project-ref.supabase.co`
- AnonKey: `your-anon-key`

You'll need to update these in `lib/config/supabase_config.dart` for development.

## Security Notes

- ✅ Configuration files are in `.gitignore`
- ✅ Environment variables are used in production
- ✅ Anon keys are safe to use in client applications
- ❌ Never commit actual credentials to Git
- ❌ Never use service role keys in client applications
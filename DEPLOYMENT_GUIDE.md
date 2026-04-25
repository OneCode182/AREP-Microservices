# AREP Twitter — AWS Deployment Guide

**Status:** ✅ READY FOR AWS DEPLOYMENT  
**Last Updated:** April 25, 2026  
**All Compilations:** COMPLETE

---

## ✅ Pre-Deployment Checklist

- [x] Monolith JAR compiled (57MB) → `monolith/target/twitter-monolith-0.0.1-SNAPSHOT.jar`
- [x] Frontend built → `frontend/dist/` (323kB+)
- [x] 3 Lambda JARs compiled:
  - [x] `microservices/user-function/target/user-function-0.0.1-SNAPSHOT.jar`
  - [x] `microservices/posts-function/target/posts-function-0.0.1-SNAPSHOT.jar`
  - [x] `microservices/stream-function/target/stream-function-0.0.1-SNAPSHOT.jar`
- [x] SAM template fixed (CodeUri paths corrected)
- [x] SAM build completed → `.aws-sam/build/template.yaml`
- [x] AWS CLI configured ✅
- [x] SAM CLI available ✅
- [x] AWS credentials VALID ✅

---

## 🚀 DEPLOYMENT STEPS (Step-by-Step)

### Step 1: Navigate to Infrastructure Directory
```bash
cd D:\AREP\AREP-Microservices\infrastructure
```

### Step 2: Deploy to AWS (Interactive)
```bash
"C:\Program Files\Amazon\AWSSAMCLI\bin\sam.cmd" deploy --guided
```

### Step 3: Answer SAM Deploy Prompts

When prompted, answer as follows:

```
Stack Name [sam-app]: arep-twitter
Region [us-west-2]: us-west-2
Confirm changes before deploy [y/N]: y
Allow SAM CLI IAM role creation [Y/n]: Y
Save parameters to samconfig.toml [Y/n]: Y
```

**Important:** Answer `y` to "Confirm changes before deploy"

### Step 4: Verify Deployment Output

After deployment completes, you'll see outputs like:

```
Outputs:
  ApiEndpoint: https://[api-id].execute-api.us-west-2.amazonaws.com/prod
  PostsTableName: arep-twitter-posts
```

**Save the ApiEndpoint URL** — you'll need this for the frontend.

---

## 📋 What SAM Deploy Will Create

1. **DynamoDB Table:** `arep-twitter-posts`
   - Hash key: `id`
   - Sort key: `createdAt`
   - On-demand billing

2. **Lambda Functions:**
   - `arep-twitter-UserFunction` → GET /api/me
   - `arep-twitter-PostsFunction` → POST /api/posts
   - `arep-twitter-StreamFunction` → GET /api/stream

3. **API Gateway:** 
   - Endpoint: `https://[api-id].execute-api.us-west-2.amazonaws.com/prod`
   - Routes: /api/me, /api/posts, /api/stream

4. **CloudFormation Stack:**
   - Name: `arep-twitter`

---

## 📝 After Deployment: Update Frontend

Once `sam deploy --guided` completes:

1. **Get the API Endpoint** from the output (e.g., `https://abc123.execute-api.us-west-2.amazonaws.com/prod`)

2. **Update frontend/.env:**
   ```bash
   cd ../frontend
   # Edit .env and change:
   VITE_API_URL=https://[api-id].execute-api.us-west-2.amazonaws.com/prod
   ```

3. **Rebuild frontend:**
   ```bash
   npm run build
   ```

4. **Upload to S3 (if hosting frontend):**
   ```bash
   aws s3 sync dist/ s3://[your-bucket-name]/
   ```

---

## 🔍 Testing After Deployment

### Test public endpoint (no auth required):
```bash
curl https://[api-id].execute-api.us-west-2.amazonaws.com/prod/api/stream
```
Expected: Empty array `[]` (no posts yet)

### Test protected endpoint (requires Auth0 token):
```bash
# First, get a token from Auth0 (via frontend login)
curl -H "Authorization: Bearer [YOUR_AUTH0_TOKEN]" \
  https://[api-id].execute-api.us-west-2.amazonaws.com/prod/api/me
```

---

## 📊 Cost Estimates (AWS)

- **Lambda:** ~$0.20/million invocations (free tier: 1M/month)
- **DynamoDB:** ~$1.25/million write units + $0.25/million read units (free tier: 25GB storage)
- **API Gateway:** $3.50/million requests (free tier: 1M/month)
- **S3:** ~$0.023/GB (if hosting frontend)

**Estimate:** Free tier covers ~1M API calls/month. Actual costs minimal unless at scale.

---

## ⚠️ Common Issues & Solutions

### Issue: "InvalidClientTokenId: The security token included in the request is invalid"
**Solution:** AWS credentials expired. Get new temporary credentials from AWS Console.

### Issue: "Unable to find a supported build workflow"
**Solution:** Template.yaml CodeUri paths were incorrect. ✅ ALREADY FIXED

### Issue: "MissingProperty error: Property Auth0Domain is missing"
**Solution:** SAM will prompt for Auth0 configuration parameters. You may need to provide:
- `Auth0Domain=onecode1.us.auth0.com`
- `Auth0Audience=https://onecode1.us.auth0.com/api/v2/`

---

## 📚 Next Steps After Full Deployment

1. ✅ **Backend deployed** (Lambda + DynamoDB + API Gateway)
2. ⏳ **Frontend deployed** (S3 + CloudFront, optional)
3. ⏳ **Update Auth0 callback URLs** to include S3 website URL
4. ⏳ **Record video demo** (optional, +5% bonus)
5. ⏳ **Submit to AREP** grading system

---

## 🎯 Quick Reference

| Component | Status | Command |
|-----------|--------|---------|
| **Monolith** | ✅ Compiled | `mvn spring-boot:run` |
| **Frontend** | ✅ Built | `npm run dev` |
| **Lambda** | ✅ Compiled | `sam local invoke` |
| **SAM Template** | ✅ Valid | `sam validate` |
| **SAM Build** | ✅ Complete | `.aws-sam/build/` exists |
| **SAM Deploy** | 🔄 Ready | `sam deploy --guided` |

---

## ✍️ Final Checklist Before Running `sam deploy --guided`

- [ ] Credentials configured: `aws sts get-caller-identity` returns valid response
- [ ] In correct directory: `pwd` shows `.../infrastructure`
- [ ] SAM CLI available: `sam --version` works
- [ ] All JARs compiled and present
- [ ] `.aws-sam/build/` exists from `sam build`
- [ ] Ready to answer 5 prompts during `sam deploy --guided`

---

**When you're ready: Run `sam deploy --guided` from `D:\AREP\AREP-Microservices\infrastructure`**

All compilation and validation is DONE. ✅

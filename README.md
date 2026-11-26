<div align="center">

# 🏋️ SUIVICOACH

**مساعدك الشخصي لإدارة التدريب والمتدربين في الجزائر**

تطبيق أندرويد احترافي للكوتشات الرياضيين لمتابعة الاشتراكات، المدفوعات، والتواصل مع المتدربين

[![Download APK](https://img.shields.io/badge/📥_تحميل_التطبيق-APK-blue?style=for-the-badge)](https://github.com/xbarryv20-wq/SuiviCoach/releases/latest)

</div>

---

## 📋 المحتويات

- [نبذة عن التطبيق](#-نبذة-عن-التطبيق)
- [تحميل وتثبيت التطبيق](#-تحميل-وتثبيت-التطبيق)
- [إعداد قاعدة البيانات Supabase](#-إعداد-قاعدة-البيانات-supabase)
- [إنشاء حساب وتسجيل الدخول](#-إنشاء-حساب-وتسجيل-الدخول)
- [إعداد الملف الشخصي](#-إعداد-الملف-الشخصي)
- [إدارة قوالب الباقات](#-إدارة-قوالب-الباقات)
- [إضافة وإدارة المتدربين](#-إضافة-وإدارة-المتدربين)
- [متابعة الاشتراكات وتجديدها](#-متابعة-الاشتراكات-وتجديدها)
- [تحصيل المدفوعات](#-تحصيل-المدفوعات)
- [إرسال تذكيرات واتساب](#-إرسال-تذكيرات-واتساب)
- [الإعدادات السريعة](#-الإعدادات-السريعة)
- [لوحة التحكم الرئيسية](#-لوحة-التحكم-الرئيسية)
- [المالية والتقارير](#-المالية-والتقارير)
- [حول المطور](#-حول-المطور)

---

## 🎯 نبذة عن التطبيق

**SUIVICOACH** تطبيق مصمم خصيصاً للكوتشات الرياضيين في الجزائر. يساعدك على:

- ✅ متابعة جميع المتدربين واشتراكاتهم
- ✅ تحصيل المدفوعات بالدينار الجزائري (DA) عبر نقداً، بريدي موب، CCP، أو تحويل بنكي
- ✅ حساب المبالغ المتبقية تلقائياً (حتى لو دفع أكثر من المطلوب لتغطية ديون سابقة)
- ✅ إرسال تذكيرات واتساب تلقائية مع المبلغ المتبقي
- ✅ الوضع الداكن والفاتح + 3 لغات (فرنسية، عربية، إنجليزية)
- ✅ مزامنة جميع البيانات مع Supabase (سحابي وآمن)

---

## 📥 تحميل وتثبيت التطبيق

1. **افتح صفحة الإصدارات:** [github.com/xbarryv20-wq/SuiviCoach/releases](https://github.com/xbarryv20-wq/SuiviCoach/releases/latest)
2. **حمّل ملف** `SUIVICOACH.apk`
3. **افتح الملف** على هاتفك الأندرويد
4. إذا ظهرت رسالة "مصدر غير معروف"، اضغط **الإعدادات** ← فعّل **السماح من هذا المصدر**
5. اضغط **تثبيت** ← **فتح**

> 💡 التطبيق يعمل على أندرويد 7.0 فما فوق (API 24+)

---

## 🗄️ إعداد قاعدة البيانات Supabase

التطبيق يحتاج قاعدة بيانات Supabase لحفظ بياناتك. اتبع الخطوات التالية:

### الخطوة 1: إنشاء مشروع Supabase

1. اذهب إلى [supabase.com](https://supabase.com) وأنشئ حساباً مجانياً
2. اضغط **New Project** واختر اسماً (مثلاً: `suivicoach`)
3. اختر كلمة مرور لقاعدة البيانات واحفظها
4. انتظر حتى يتم إنشاء المشروع

### الخطوة 2: إنشاء الجداول

1. في لوحة تحكم Supabase، اذهب إلى **SQL Editor**
2. انسخ والصق **كامل** هذا الكود SQL واضغط **Run**:

<details>
<summary>اضغط هنا لعرض كود SQL الكامل</summary>

```sql
create extension if not exists "pgcrypto";

create table if not exists public.coach_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  business_name text not null default 'SUIVICOACH Studio',
  phone text not null default '',
  email text not null default '',
  avatar_url text not null default '',
  created_at timestamptz not null default now()
);

alter table public.coach_profiles add column if not exists avatar_url text not null default '';

create or replace function public.handle_new_coach_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.coach_profiles (id, full_name, email)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'full_name', ''), coalesce(new.email, ''))
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users for each row execute procedure public.handle_new_coach_user();

create table if not exists public.plan_templates (
  id text primary key,
  coach_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  duration_days integer not null default 30,
  price_da integer not null default 0,
  sessions_total integer null,
  created_at timestamptz not null default now()
);
create index if not exists idx_plan_templates_coach_id on public.plan_templates(coach_id);

create table if not exists public.clients (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  full_name text not null,
  phone text not null default '',
  email text not null default '',
  goal text not null default '',
  notes text not null default '',
  is_archived boolean not null default false,
  created_at timestamptz not null default now()
);
create index if not exists idx_clients_coach_id on public.clients(coach_id);

create table if not exists public.subscriptions (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  client_id uuid not null references public.clients(id) on delete cascade,
  plan_name text not null,
  price_da integer not null default 0,
  duration_days integer not null default 30,
  start_date date not null,
  end_date date not null,
  sessions_total integer null,
  sessions_used integer not null default 0,
  amount_paid_da integer not null default 0,
  notes text not null default '',
  created_at timestamptz not null default now()
);
create index if not exists idx_subscriptions_coach_id on public.subscriptions(coach_id);
create index if not exists idx_subscriptions_client_id on public.subscriptions(client_id);
create index if not exists idx_subscriptions_end_date on public.subscriptions(end_date);

create table if not exists public.payments (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  client_id uuid not null references public.clients(id) on delete cascade,
  subscription_id uuid null references public.subscriptions(id) on delete set null,
  amount_da integer not null check (amount_da > 0),
  paid_at timestamptz not null default now(),
  method text not null default 'cash' check (method in ('cash', 'baridimob', 'ccp', 'transfer')),
  note text not null default ''
);
create index if not exists idx_payments_coach_id on public.payments(coach_id);
create index if not exists idx_payments_client_id on public.payments(client_id);

alter table public.coach_profiles enable row level security;
alter table public.plan_templates enable row level security;
alter table public.clients enable row level security;
alter table public.subscriptions enable row level security;
alter table public.payments enable row level security;

drop policy if exists "coach_profiles_select_own" on public.coach_profiles;
create policy "coach_profiles_select_own" on public.coach_profiles for select using (auth.uid() = id);
drop policy if exists "coach_profiles_insert_own" on public.coach_profiles;
create policy "coach_profiles_insert_own" on public.coach_profiles for insert with check (auth.uid() = id);
drop policy if exists "coach_profiles_update_own" on public.coach_profiles;
create policy "coach_profiles_update_own" on public.coach_profiles for update using (auth.uid() = id) with check (auth.uid() = id);

drop policy if exists "plan_templates_all_own" on public.plan_templates;
create policy "plan_templates_all_own" on public.plan_templates for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);

drop policy if exists "clients_all_own" on public.clients;
create policy "clients_all_own" on public.clients for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);

drop policy if exists "subscriptions_all_own" on public.subscriptions;
create policy "subscriptions_all_own" on public.subscriptions for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);

drop policy if exists "payments_all_own" on public.payments;
create policy "payments_all_own" on public.payments for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);
```

</details>

### الخطوة 3: الحصول على مفاتيح API

1. اذهب إلى **Project Settings** ← **API**
2. انسخ:
   - **Project URL** (مثال: `https://xxxxx.supabase.co`)
   - **anon public key** (المفتاح العام)

> ⚠️ لا تستخدم **service_role key** أبداً في التطبيق

---

## 🔐 إنشاء حساب وتسجيل الدخول

عند فتح التطبيق لأول مرة، سترى شاشة تسجيل الدخول:

### إنشاء حساب جديد:
1. اضغط على تبويب **إنشاء حساب**
2. أدخل **الاسم الكامل** (مثال: Coach Youcef)
3. أدخل **البريد الإلكتروني**
4. أدخل **كلمة مرور** قوية
5. اضغط **إنشاء حساب**

### تسجيل الدخول:
1. أدخل بريدك الإلكتروني وكلمة المرور
2. اضغط **تسجيل الدخول**

### نسيت كلمة المرور؟
1. اضغط **نسيت كلمة المرور؟**
2. أدخل بريدك الإلكتروني
3. ستصلك رسالة لإعادة تعيين كلمة المرور

---

## 👤 إعداد الملف الشخصي

بعد تسجيل الدخول، اذهب إلى **الإعدادات** (أيقونة ⚙️):

1. **الصورة الشخصية**: اضغط على الدائرة الرمادية لاختيار صورة من معرض الصور
2. **الاسم الكامل**: اكتب اسمك كما تريد أن يظهر
3. **اسم الاستوديو**: اكتب اسم نادي التدريب أو علامتك التجارية
4. **رقم الهاتف**: أدخل رقمك
5. اضغط **تحديث الملف الشخصي**

> 💡 صورتك الشخصية ستظهر في أعلى يسار لوحة التحكم الرئيسية بدلاً من الشعار

---

## 📦 إدارة قوالب الباقات

قوالب الباقات هي الاشتراكات الجاهزة التي تقدمها لمتدربيك. اذهب إلى **الإعدادات** ← **قوالب الباقات**:

### إضافة باقة جديدة:
1. اضغط زر **+**
2. أدخل:
   - **اسم الباقة** (مثال: "Coaching 1-on-1 شهر")
   - **المدة بالأيام** (مثال: 30)
   - **السعر بالدينار** (مثال: 5000)
   - **عدد الحصص** (اختياري، مثال: 12)
3. اضغط **حفظ**

### حذف باقة:
اضغط على أيقونة 🗑️ بجانب الباقة

### القوالب الافتراضية:
| الباقة | المدة | السعر | الحصص |
|--------|-------|-------|-------|
| Coaching 1-on-1 (شهر) | 30 يوم | 5,000 DA | 12 |
| Pack Transformation (3 أشهر) | 90 يوم | 13,500 DA | 36 |
| Pack 12 حصة | 45 يوم | 6,000 DA | 12 |
| متابعة Online + تغذية | 30 يوم | 3,500 DA | غير محدود |

> 💡 القوالب تتزامن تلقائياً مع Supabase

---

## 👥 إضافة وإدارة المتدربين

### إضافة متدرب جديد:
1. اضغط زر **+** الأزرق (في لوحة التحكم أو صفحة المتدربين)
2. أدخل معلومات المتدرب:
   - **الاسم الكامل** (إلزامي)
   - **رقم الهاتف**
   - **البريد الإلكتروني**
   - **الهدف** (مثال: تضخيم، تنشيف، لياقة)
   - **ملاحظات** التدريب والصحة
3. اختياري: فعّل **إضافة اشتراك** لربط باقة فوراً
4. اضغط **حفظ**

### تعديل أو حذف متدرب:
1. اضغط على بطاقة المتدرب لفتح ملفه
2. **تعديل**: اضغط أيقونة التعديل
3. **حذف**: اضغط أيقونة الحذف (تأكيد مطلوب)

### البحث والفلترة:
- **شريط البحث**: ابحث بالاسم، الهاتف، أو الهدف
- **الفلاتر**: الكل، نشط، ينتهي قريباً، منتهي، غير مسدد

---

## 🔄 متابعة الاشتراكات وتجديدها

### حالات الاشتراك:
| الحالة | اللون | المعنى |
|--------|-------|--------|
| 🟢 نشط | أخضر | أكثر من 3 أيام متبقية |
| 🟡 ينتهي قريباً | برتقالي | 1 إلى 3 أيام متبقية |
| 🔴 منتهي | أحمر | انتهت صلاحية الاشتراك |

### تجديد اشتراك:
1. افتح ملف المتدرب
2. اضغط **تجديد**
3. اختر باقة من القوالب الجاهزة أو أدخل باقة مخصصة
4. حدد:
   - **تاريخ البداية** (من اليوم أو من نهاية الاشتراك السابق)
   - **المبلغ المدفوع** (يمكن أن يكون جزئياً)
   - **طريقة الدفع**
5. اضغط **تأكيد**

---

## 💰 تحصيل المدفوعات

### كيف يعمل حساب المتبقي:
التطبيق يحسب المبلغ المتبقي **عبر جميع الاشتراكات** وليس فقط الاشتراك الحالي:

**مثال عملي:**
- المتدرب عنده اشتراك شهر بـ 5,000 DA
- دفع 3,500 DA فقط ← **متبقي 1,500 DA**
- جدّد اشتراك جديد بـ 5,000 DA ودفع 6,500 DA ← يغطي الاشتراك الجديد (5,000) + الباقي القديم (1,500) ← **متبقي 0 DA** ✅

### تسجيل دفعة:
1. افتح ملف المتدرب
2. اضغط **تحصيل دفعة**
3. أدخل **المبلغ** بالدينار
4. اختر **طريقة الدفع**: نقداً، بريدي موب، CCP، أو تحويل بنكي
5. أضف ملاحظة (اختياري)
6. اضغط **تأكيد**

---

## 📱 إرسال تذكيرات واتساب

عندما يقترب اشتراك متدرب من الانتهاء أو انتهى فعلاً:

1. افتح ملف المتدرب
2. اضغط زر **واتساب** الأخضر
3. التطبيق سيفتح واتساب تلقائياً مع رسالة جاهزة تتضمن:
   - اسم المتدرب
   - اسم الباقة
   - تاريخ الانتهاء
   - **المبلغ المتبقي** (إن وجد)

**مثال رسالة:**
> Salam أحمد ! Petit rappel : ton abonnement coaching (Coaching 1-on-1) expire le 28/11/2026 (dans 3 j). Pense au renouvellement ! Il te reste 1 500 DA à régler.

---

## ⚡ الإعدادات السريعة

في أعلى يمين لوحة التحكم، اضغط على زر **⋮** (ثلاث نقاط) لفتح القائمة السريعة:

- **اللغة**: اختر بين الفرنسية (FR)، العربية (AR)، والإنجليزية (EN)
- **المظهر**: بدّل بين الوضع الداكن والفاتح

> 💡 يمكنك أيضاً تغيير هذه الإعدادات من صفحة **الإعدادات**

---

## 📊 لوحة التحكم الرئيسية

لوحة التحكم تعرض لك نظرة شاملة:

- **صورتك الشخصية** في أعلى اليسار
- **إيرادات الشهر** بالدينار
- **3 مؤشرات**: متدربون نشطون، ينتهي قريباً، منتهيون
- **التجديدات العاجلة**: المتدربون الذين يحتاجون تجديداً فوراً
- **آخر المدفوعات**: سجل آخر التحصيلات

---

## 📈 المالية والتقارير

صفحة **المالية** تعرض:

- **إجمالي المحصل**: مجموع جميع المدفوعات
- **المبالغ المتبقية**: إجمالي ما لم يُسدد بعد
- **التوزيع حسب وسيلة الدفع**: نقداً، بريدي موب، CCP، تحويل بنكي
- **سجل التحصيلات**: كل دفعة بالتفصيل

### تصدير CSV:
اضغط **تصدير CSV** لمشاركة تقرير المدفوعات عبر أي تطبيق

---

## 👨‍💻 حول المطور

| | |
|---|---|
| **المطور** | Aoussedj Abdelbari |
| **GitHub** | [@xbarryv20-wq](https://github.com/xbarryv20-wq) |
| **التقنيات** | Kotlin, Jetpack Compose, Supabase |

---

## 📄 الترخيص

هذا المشروع مفتوح المصدر.

---

<div align="center">

صُنع بـ ❤️ في الجزائر 🇩🇿

</div>

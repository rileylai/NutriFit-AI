-- Statement removes any existing foreign key to avoid conflicts when reapplying the constraint.
ALTER TABLE public.workouts DROP CONSTRAINT IF EXISTS workouts_user_id_fkey;
-- Statement creates the foreign key linking workouts.user_id to the primary key in the user table.
ALTER TABLE public.workouts ADD CONSTRAINT workouts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(user_id);
-- Note reminds operators to run this SQL within the Supabase SQL editor or migration tooling.
-- Execute the above SQL inside Supabase to enforce referential integrity between workouts and user tables.

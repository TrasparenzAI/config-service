-- Duplica la root_rule presente nel workflow.cron.body nella nuova chiave workflow.cron.root_rule,
-- in modo che possa essere letta senza dover accedere all'intero workflow.cron.body
INSERT INTO properties (version, application, profile, label, "KEY", "VALUE")
SELECT 0, p.application, p.profile, p.label, 'workflow.cron.root_rule', p."VALUE"::jsonb -> 'input' ->> 'root_rule'
FROM properties p
WHERE p."KEY" = 'workflow.cron.body'
  AND p."VALUE"::jsonb -> 'input' ->> 'root_rule' IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM properties r
                  WHERE r."KEY" = 'workflow.cron.root_rule'
                    AND r.application = p.application
                    AND r.profile IS NOT DISTINCT FROM p.profile
                    AND r.label IS NOT DISTINCT FROM p.label);

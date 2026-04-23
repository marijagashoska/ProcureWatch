-- Optional cleanup for repeatable local runs
DELETE FROM realized_contracts;
DELETE FROM decisions;
DELETE FROM contracts;
DELETE FROM notices;
DELETE FROM procurement_plan_items;
DELETE FROM procurement_plans;
DELETE FROM suppliers;
DELETE FROM institutions;

-- =========================
-- INSTITUTIONS
-- =========================
INSERT INTO institutions (
    id, external_id, official_name, normalized_name, institution_type, city, postal_code, category, source_url
) VALUES
      (1, 'INST-001', 'Ministry of Health', 'ministry of health', 'MINISTRY', 'Skopje', '1000', 'HEALTH', 'https://example.local/institutions/1'),
      (2, 'INST-002', 'Municipality of Berovo', 'municipality of berovo', 'MUNICIPALITY', 'Berovo', '2330', 'LOCAL_GOVERNMENT', 'https://example.local/institutions/2'),
      (3, 'INST-003', 'Public Water Supply Company', 'public water supply company', 'PUBLIC_ENTERPRISE', 'Skopje', '1000', 'UTILITIES', 'https://example.local/institutions/3');

-- =========================
-- SUPPLIERS
-- =========================
INSERT INTO suppliers (
    id, external_id, official_name, normalized_name, real_owners_info, source_url
) VALUES
      (1, 'SUP-001', 'MedTech DOOEL', 'medtech dooel', 'Owner: Example Holding', 'https://example.local/suppliers/1'),
      (2, 'SUP-002', 'BuildPro DOO', 'buildpro doo', 'Owner: Civil Group', 'https://example.local/suppliers/2'),
      (3, 'SUP-003', 'Aqua Systems DOOEL', 'aqua systems dooel', 'Owner: Aqua Group', 'https://example.local/suppliers/3'),
      (4, 'SUP-004', 'Office Plus DOO', 'office plus doo', 'Owner: Office Group', 'https://example.local/suppliers/4');

-- =========================
-- PROCUREMENT PLANS
-- =========================
INSERT INTO procurement_plans (
    id, institution_id, plan_year, publication_date, source_url
) VALUES
      (1, 1, 2024, '2024-01-15', 'https://example.local/plans/1'),
      (2, 2, 2024, '2024-01-20', 'https://example.local/plans/2'),
      (3, 3, 2024, '2024-01-25', 'https://example.local/plans/3');

-- =========================
-- PLAN ITEMS
-- =========================
INSERT INTO procurement_plan_items (
    id, plan_id, subject, cpv_code, contract_type, procedure_type, expected_start_month, has_notice, notes, source_url
) VALUES
      (1, 1, 'Procurement of MRI equipment', '33110000', 'GOODS', 'OPEN', 'APRIL', TRUE, 'Capital medical equipment', 'https://example.local/plan-items/1'),
      (2, 1, 'Ambulance maintenance service', '50100000', 'SERVICES', 'NEGOTIATED', 'FEBRUARY', FALSE, 'Intentional test case: hasNotice=false', 'https://example.local/plan-items/2'),
      (3, 2, 'Local road maintenance', '45233141', 'WORKS', 'OPEN', 'MAY', TRUE, 'Municipal road works', 'https://example.local/plan-items/3'),
      (4, 3, 'Water pumps procurement', '42122130', 'GOODS', 'OPEN', 'JUNE', TRUE, 'Pumping equipment', 'https://example.local/plan-items/4'),
      (5, 1, 'Laboratory reagents', '33696500', 'GOODS', 'OPEN', 'MARCH', TRUE, 'Consumables', 'https://example.local/plan-items/5'),
      (6, 1, 'Diagnostic kits', '33141625', 'GOODS', 'OPEN', 'APRIL', TRUE, 'Repeated supplier pattern seed', 'https://example.local/plan-items/6'),
      (7, 1, 'Ultrasound accessories', '33124120', 'GOODS', 'OPEN', 'JUNE', TRUE, 'Repeated supplier pattern seed', 'https://example.local/plan-items/7'),
      (8, 2, 'Street lighting maintenance', '50232100', 'SERVICES', 'OPEN', 'JULY', TRUE, 'Normal contract case', 'https://example.local/plan-items/8');

-- =========================
-- NOTICES
-- =========================
INSERT INTO notices (
    id, institution_id, plan_item_id, notice_number, subject, contract_type, procedure_type, publication_date, deadline_date, source_url
) VALUES
      (1, 1, 1, 'N-2024-001', 'Procurement of MRI equipment', 'GOODS', 'OPEN', '2024-02-01', '2024-02-20 12:00:00', 'https://example.local/notices/1'),
      (2, 1, 2, 'N-2024-002', 'Ambulance maintenance service', 'SERVICES', 'NEGOTIATED', '2024-01-10', '2024-01-15 12:00:00', 'https://example.local/notices/2'),
      (3, 2, 3, 'N-2024-003', 'Local road maintenance', 'WORKS', 'OPEN', '2024-03-05', '2024-03-25 12:00:00', 'https://example.local/notices/3'),
      (4, 3, 4, 'N-2024-004', 'Water pumps procurement', 'GOODS', 'OPEN', '2024-04-01', '2024-04-20 12:00:00', 'https://example.local/notices/4'),
      (5, 1, 5, 'N-2024-005', 'Laboratory reagents', 'GOODS', 'OPEN', '2024-02-15', '2024-03-01 12:00:00', 'https://example.local/notices/5'),
      (6, 1, 6, 'N-2024-006', 'Diagnostic kits', 'GOODS', 'OPEN', '2024-04-01', '2024-04-15 12:00:00', 'https://example.local/notices/6'),
      (7, 1, 7, 'N-2024-007', 'Ultrasound accessories', 'GOODS', 'OPEN', '2024-06-01', '2024-06-15 12:00:00', 'https://example.local/notices/7'),
      (8, 2, 8, 'N-2024-008', 'Street lighting maintenance', 'SERVICES', 'OPEN', '2024-06-10', '2024-06-25 12:00:00', 'https://example.local/notices/8');

-- =========================
-- CONTRACTS
-- =========================
-- Contract 1: should trigger many flags in your rule engine
INSERT INTO contracts (
    id, institution_id, supplier_id, notice_number, subject, contract_type, procedure_type,
    contract_date, publication_date, estimated_value_vat, contract_value_vat, currency, source_url
) VALUES
      (1, 1, 1, 'N-2024-001', 'Procurement of MRI equipment', 'GOODS', 'OPEN',
       '2024-06-15', '2024-06-20', 1000000.00, 1800000.00, 'MKD', 'https://example.local/contracts/1'),

-- Contract 2: same supplier + same institution pattern
      (2, 1, 1, 'N-2024-005', 'Laboratory reagents', 'GOODS', 'OPEN',
       '2024-03-20', '2024-03-25', 300000.00, 290000.00, 'MKD', 'https://example.local/contracts/2'),

-- Contract 3: same supplier + same institution pattern
      (3, 1, 1, 'N-2024-006', 'Diagnostic kits', 'GOODS', 'OPEN',
       '2024-05-10', '2024-05-12', 200000.00, 210000.00, 'MKD', 'https://example.local/contracts/3'),

-- Contract 4: same supplier + same institution pattern
      (4, 1, 1, 'N-2024-007', 'Ultrasound accessories', 'GOODS', 'OPEN',
       '2024-07-10', '2024-07-12', 150000.00, 145000.00, 'MKD', 'https://example.local/contracts/4'),

-- Contract 5: missing notice link + no decision
      (5, 2, 2, 'MISSING-999', 'Emergency road repair', 'WORKS', 'NEGOTIATED',
       '2024-08-01', '2024-08-05', 500000.00, 520000.00, 'MKD', 'https://example.local/contracts/5'),

-- Contract 6: realized well below awarded
      (6, 3, 3, 'N-2024-004', 'Water pumps procurement', 'GOODS', 'OPEN',
       '2024-04-25', '2024-05-01', 400000.00, 380000.00, 'MKD', 'https://example.local/contracts/6'),

-- Contract 7: linked to plan item with hasNotice=false
      (7, 1, 4, 'N-2024-002', 'Ambulance maintenance service', 'SERVICES', 'NEGOTIATED',
       '2024-02-28', '2024-03-01', 120000.00, 118000.00, 'MKD', 'https://example.local/contracts/7'),

-- Contract 8: relatively normal contract
      (8, 2, 2, 'N-2024-008', 'Street lighting maintenance', 'SERVICES', 'OPEN',
       '2024-07-05', '2024-07-07', 250000.00, 245000.00, 'MKD', 'https://example.local/contracts/8');

-- =========================
-- DECISIONS
-- =========================
INSERT INTO decisions (
    id, notice_id, contract_id, institution_id, supplier_id, notice_number, decision_date,
    subject, decision_text, procedure_type, source_url
) VALUES
      (1, 1, 1, 1, 1, 'N-2024-001', '2024-05-01',
       'Procurement of MRI equipment', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/1'),

      (2, 5, 2, 1, 1, 'N-2024-005', '2024-03-10',
       'Laboratory reagents', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/2'),

      (3, 6, 3, 1, 1, 'N-2024-006', '2024-04-20',
       'Diagnostic kits', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/3'),

      (4, 7, 4, 1, 1, 'N-2024-007', '2024-06-20',
       'Ultrasound accessories', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/4'),

      (5, 4, 6, 3, 3, 'N-2024-004', '2024-04-15',
       'Water pumps procurement', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/5'),

      (6, 2, 7, 1, 4, 'N-2024-002', '2024-02-10',
       'Ambulance maintenance service', 'Supplier selected under negotiated procedure.', 'NEGOTIATED', 'https://example.local/decisions/6'),

      (7, 8, 8, 2, 2, 'N-2024-008', '2024-06-28',
       'Street lighting maintenance', 'Supplier selected after evaluation.', 'OPEN', 'https://example.local/decisions/7');

-- =========================
-- REALIZED CONTRACTS
-- =========================
INSERT INTO realized_contracts (
    id, institution_id, supplier_id, contract_id, notice_number, subject, contract_type, procedure_type,
    awarded_value_vat, realized_value_vat, paid_value_vat, publication_date, republish_date, source_url
) VALUES
-- Contract 1: realized > awarded, paid != realized
(1, 1, 1, 1, 'N-2024-001', 'Procurement of MRI equipment', 'GOODS', 'OPEN',
 1800000.00, 2300000.00, 2100000.00, '2024-09-01', '2024-09-10', 'https://example.local/realized-contracts/1'),

-- Contract 6: realized << awarded
(2, 3, 3, 6, 'N-2024-004', 'Water pumps procurement', 'GOODS', 'OPEN',
 380000.00, 150000.00, 150000.00, '2024-07-01', '2024-07-05', 'https://example.local/realized-contracts/2'),

-- Contract 8: normal realized contract
(3, 2, 2, 8, 'N-2024-008', 'Street lighting maintenance', 'SERVICES', 'OPEN',
 245000.00, 244000.00, 244000.00, '2024-09-15', '2024-09-20', 'https://example.local/realized-contracts/3');
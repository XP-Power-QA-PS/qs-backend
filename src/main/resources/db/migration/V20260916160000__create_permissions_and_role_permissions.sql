-- Migration: Create permissions and role_permissions tables, seed factory permissions and role mappings
-- Version: 20260916160000

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Seed 23 granular manufacturing permissions with 18-digit Snowflake IDs
INSERT INTO permissions (id, code, module, action, name, description, created_by, created_at)
VALUES
    -- Module 1: Go/No-Go & Daily Testing (GONOGO)
    (272280320496112001, 'GONOGO_VIEW', 'GONOGO', 'VIEW', 'Xem nhật ký thử nghiệm', 'Xem kết quả kiểm tra Go/No-Go và lịch sử kiểm định hàng ngày', 'SYSTEM', NOW()),
    (272280320496112002, 'GONOGO_RECORD', 'GONOGO', 'RECORD', 'Ghi nhận lần thử (Record Attempt)', 'Nhập thông số thử nghiệm và kết quả PASS/FAIL theo ca', 'SYSTEM', NOW()),
    (272280320496112003, 'GONOGO_VERIFY', 'GONOGO', 'VERIFY', 'Xác nhận kiểm định máy (Machine Verify)', 'Kiểm định và ký xác thực máy móc thiết bị đo đầu ca', 'SYSTEM', NOW()),
    (272280320496112004, 'GONOGO_OVERRIDE', 'GONOGO', 'OVERRIDE', 'Ký duyệt vượt chuẩn (Supervisor Override)', 'Phê duyệt ngoại lệ hoặc ký xác nhận kết quả ngoài quy chuẩn', 'SYSTEM', NOW()),
    (272280320496112005, 'GONOGO_EXPORT', 'GONOGO', 'EXPORT', 'Xuất báo cáo thử nghiệm', 'Xuất dữ liệu thử nghiệm hàng ngày/tháng ra file Excel/PDF', 'SYSTEM', NOW()),

    -- Module 2: Equipments & Floors (EQUIPMENT)
    (272280320496112006, 'EQUIPMENT_VIEW', 'EQUIPMENT', 'VIEW', 'Xem thiết bị & tầng xưởng', 'Xem danh mục máy móc, thông số kiểm tra và sơ đồ chuyền', 'SYSTEM', NOW()),
    (272280320496112007, 'EQUIPMENT_MANAGE', 'EQUIPMENT', 'MANAGE', 'Quản lý thiết bị (Thêm/Sửa/Xóa)', 'Thêm mới, cấu hình chương trình kiểm tra hoặc xóa máy', 'SYSTEM', NOW()),
    (272280320496112008, 'FLOOR_MANAGE', 'EQUIPMENT', 'FLOOR', 'Quản lý mặt bằng & tầng xưởng', 'Tạo mới, chỉnh sửa thông tin tầng hoặc phân bổ khu vực', 'SYSTEM', NOW()),
    (272280320496112009, 'EQUIPMENT_CALIBRATE', 'EQUIPMENT', 'CALIBRATE', 'Hiệu chuẩn & Cập nhật trạng thái', 'Cập nhật hạn hiệu chuẩn và chuyển đổi trạng thái vận hành thiết bị', 'SYSTEM', NOW()),

    -- Module 3: Customer Complaints (COMPLAINT)
    (272280320496112010, 'COMPLAINT_VIEW', 'COMPLAINT', 'VIEW', 'Xem hồ sơ khiếu nại', 'Xem danh sách và chi tiết các phản ánh lỗi từ khách hàng', 'SYSTEM', NOW()),
    (272280320496112011, 'COMPLAINT_CREATE', 'COMPLAINT', 'CREATE', 'Tạo hồ sơ khiếu nại mới', 'Tiếp nhận và mở hồ sơ khiếu nại khách hàng mới', 'SYSTEM', NOW()),
    (272280320496112012, 'COMPLAINT_UPDATE_8D', 'COMPLAINT', 'UPDATE_8D', 'Thực hiện quy trình 8D (D1-D8)', 'Phân tích nguyên nhân gốc rễ và triển khai hành động khắc phục CAPA', 'SYSTEM', NOW()),
    (272280320496112013, 'COMPLAINT_APPROVE_CLOSE', 'COMPLAINT', 'APPROVE', 'Phê duyệt & Đóng hồ sơ CAPA', 'Ký duyệt kết luận xử lý sự cố chất lượng và hoàn tất hồ sơ', 'SYSTEM', NOW()),
    (272280320496112014, 'COMPLAINT_EXPORT', 'COMPLAINT', 'EXPORT', 'Xuất báo cáo hồ sơ 8D', 'Xuất hồ sơ 8D và báo cáo khiếu nại ra tài liệu chuẩn', 'SYSTEM', NOW()),

    -- Module 4: CFT Meetings & Email Alerts (COMMUNICATION)
    (272280320496112015, 'COMMUNICATION_VIEW', 'COMMUNICATION', 'VIEW', 'Xem lịch họp & email', 'Xem danh sách lịch họp giải quyết sự cố và nhật ký gửi thông báo', 'SYSTEM', NOW()),
    (272280320496112016, 'COMMUNICATION_SCHEDULE', 'COMMUNICATION', 'SCHEDULE', 'Lên lịch họp CFT', 'Tổ chức cuộc họp chất lượng liên phòng ban và gửi thư mời', 'SYSTEM', NOW()),
    (272280320496112017, 'COMMUNICATION_SEND_MINUTES', 'COMMUNICATION', 'MINUTES', 'Gửi biên bản họp & kết luận', 'Phân phối biên bản họp và phân công nhiệm vụ tự động qua email', 'SYSTEM', NOW()),
    (272280320496112018, 'COMMUNICATION_CONFIG_ALERTS', 'COMMUNICATION', 'CONFIG_ALERTS', 'Cấu hình danh sách cảnh báo', 'Quản lý danh sách email kỹ sư/quản đốc nhận cảnh báo lỗi tự động', 'SYSTEM', NOW()),

    -- Module 5: System & Security Administration (SYSTEM_ADMIN)
    (272280320496112019, 'ADMIN_VIEW_USERS', 'SYSTEM_ADMIN', 'VIEW_USERS', 'Xem danh bạ người dùng', 'Xem danh sách nhân sự, mã nhân viên và trạng thái hoạt động', 'SYSTEM', NOW()),
    (272280320496112020, 'ADMIN_MANAGE_USERS', 'SYSTEM_ADMIN', 'MANAGE_USERS', 'Quản lý tài khoản người dùng', 'Thêm mới, cập nhật hoặc khóa tài khoản nhân viên', 'SYSTEM', NOW()),
    (272280320496112021, 'ADMIN_RESET_PASSWORD', 'SYSTEM_ADMIN', 'RESET_PWD', 'Cấp lại mật khẩu mặc định', 'Đặt lại mật khẩu nhanh cho công nhân và nhân sự xưởng', 'SYSTEM', NOW()),
    (272280320496112022, 'ADMIN_MANAGE_ROLES', 'SYSTEM_ADMIN', 'MANAGE_ROLES', 'Quản lý vai trò người dùng', 'Thêm mới hoặc chỉnh sửa mô tả danh mục vai trò', 'SYSTEM', NOW()),
    (272280320496112023, 'ADMIN_CONFIG_PERMISSIONS', 'SYSTEM_ADMIN', 'CONFIG_PERMS', 'Cấu hình ma trận phân quyền', 'Tùy biến quyền hạn chi tiết cho từng vai trò trên hệ thống', 'SYSTEM', NOW())
ON CONFLICT (id) DO NOTHING;

-- Map default permissions to roles:
-- 1. ROLE_ADMIN: Full 23 permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- 2. ROLE_SUPERVISOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPERVISOR'
  AND p.code IN (
      'GONOGO_VIEW', 'GONOGO_RECORD', 'GONOGO_VERIFY', 'GONOGO_OVERRIDE', 'GONOGO_EXPORT',
      'EQUIPMENT_VIEW', 'EQUIPMENT_MANAGE', 'EQUIPMENT_CALIBRATE',
      'COMPLAINT_VIEW', 'COMPLAINT_CREATE', 'COMPLAINT_UPDATE_8D', 'COMPLAINT_APPROVE_CLOSE', 'COMPLAINT_EXPORT',
      'COMMUNICATION_VIEW', 'COMMUNICATION_SCHEDULE', 'COMMUNICATION_SEND_MINUTES',
      'ADMIN_VIEW_USERS', 'ADMIN_RESET_PASSWORD'
  )
ON CONFLICT DO NOTHING;

-- 3. ROLE_QC_ENGINEER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_QC_ENGINEER'
  AND p.code IN (
      'GONOGO_VIEW', 'GONOGO_RECORD', 'GONOGO_VERIFY', 'GONOGO_EXPORT',
      'EQUIPMENT_VIEW', 'EQUIPMENT_CALIBRATE',
      'COMPLAINT_VIEW', 'COMPLAINT_CREATE', 'COMPLAINT_UPDATE_8D', 'COMPLAINT_EXPORT',
      'COMMUNICATION_VIEW', 'COMMUNICATION_SCHEDULE', 'COMMUNICATION_SEND_MINUTES'
  )
ON CONFLICT DO NOTHING;

-- 4. ROLE_INSPECTOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_INSPECTOR'
  AND p.code IN (
      'GONOGO_VIEW', 'GONOGO_RECORD', 'GONOGO_VERIFY',
      'EQUIPMENT_VIEW',
      'COMPLAINT_VIEW', 'COMPLAINT_CREATE',
      'COMMUNICATION_VIEW'
  )
ON CONFLICT DO NOTHING;

-- 5. ROLE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_OPERATOR'
  AND p.code IN (
      'GONOGO_VIEW', 'GONOGO_RECORD',
      'EQUIPMENT_VIEW'
  )
ON CONFLICT DO NOTHING;

-- 6. ROLE_USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER'
  AND p.code IN (
      'GONOGO_VIEW',
      'EQUIPMENT_VIEW',
      'COMPLAINT_VIEW'
  )
ON CONFLICT DO NOTHING;

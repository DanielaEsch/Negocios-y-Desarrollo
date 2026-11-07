-- Datos iniciales para desarrollo local.

INSERT INTO AREA (TX_NOMBRE, TX_DESCRIPCION, DN_USUARIO_CREADOR)
VALUES ('COCINA', 'PREPARACION DE ALIMENTOS', 0),
       ('RECEPCION', 'ATENCION A CLIENTES', 0),
       ('MANTENIMIENTO', 'LIMPIEZA E INFRAESTRUCTURA', 0)
ON CONFLICT DO NOTHING;

-- Usuarios de demostracion. Contrasena de todos: TaskIn2026 (hash BCrypt, costo 12).
-- Son personas ficticias de un negocio ficticio.
INSERT INTO USUARIO (TX_NOMBRE, TX_CORREO, TX_HASH_CONTRASENA, TX_PERFIL, DN_CONSENTIMIENTO_DATOS,
                     DD_FECHA_CONSENTIMIENTO, DN_USUARIO_CREADOR)
VALUES ('Laura Mendez', 'laura@taskin.mx', '$2a$12$nO8nnQzKAGZ7kCUKyNKyUuTuMiI6CBL1iIvf7t7QiIDHx7RuqPRcG',
        'ADMINISTRADOR', TRUE, CURRENT_TIMESTAMP, 0),
       ('Jorge Ramirez', 'jorge@taskin.mx', '$2a$12$nO8nnQzKAGZ7kCUKyNKyUuTuMiI6CBL1iIvf7t7QiIDHx7RuqPRcG',
        'SUPERVISOR', TRUE, CURRENT_TIMESTAMP, 0),
       ('Ana Torres', 'ana@taskin.mx', '$2a$12$nO8nnQzKAGZ7kCUKyNKyUuTuMiI6CBL1iIvf7t7QiIDHx7RuqPRcG',
        'OPERADOR', TRUE, CURRENT_TIMESTAMP, 0),
       ('Luis Perez', 'luis@taskin.mx', '$2a$12$nO8nnQzKAGZ7kCUKyNKyUuTuMiI6CBL1iIvf7t7QiIDHx7RuqPRcG',
        'OPERADOR', TRUE, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- Jorge supervisa las tres areas; Ana cubre mantenimiento y recepcion; Luis, cocina.
INSERT INTO USUARIO_AREA (FK_USUARIO, FK_AREA, DN_USUARIO_CREADOR)
SELECT u.PK_ID, a.PK_ID, 0
FROM USUARIO u
JOIN AREA a ON (u.TX_CORREO = 'jorge@taskin.mx')
            OR (u.TX_CORREO = 'ana@taskin.mx' AND a.TX_NOMBRE IN ('MANTENIMIENTO', 'RECEPCION'))
            OR (u.TX_CORREO = 'luis@taskin.mx' AND a.TX_NOMBRE = 'COCINA')
ON CONFLICT DO NOTHING;

-- Rutinas. La primera es la de la entrevista de campo: banos cada dos horas.
INSERT INTO RUTINA (FK_AREA, TX_NOMBRE, TX_DESCRIPCION, TX_FRECUENCIA, DD_HORA_INICIO, DD_HORA_FIN,
                    DN_DIA_SEMANA, DN_USUARIO_CREADOR)
SELECT a.PK_ID, r.nombre, r.descripcion, r.frecuencia, r.inicio::TIME, r.fin::TIME, r.dia, 0
FROM (VALUES
    ('MANTENIMIENTO', 'REVISION DE BANOS', 'BANOS DE CLIENTES Y DE PERSONAL', 'CADA_2_HORAS', '08:00', '20:00', NULL),
    ('MANTENIMIENTO', 'REVISION DE EXTINTORES', 'PRESION, SELLO Y FECHA DE RECARGA', 'SEMANAL', '09:00', NULL, 1),
    ('COCINA', 'APERTURA DE COCINA', 'ANTES DEL PRIMER SERVICIO', 'DIARIA', '07:00', NULL, NULL),
    ('COCINA', 'CONTROL DE TEMPERATURAS', 'REFRIGERADORES Y CONGELADORES', 'CADA_2_HORAS', '09:00', '21:00', NULL),
    ('COCINA', 'CIERRE DE COCINA', 'AL TERMINAR EL ULTIMO SERVICIO', 'DIARIA', '22:00', NULL, NULL),
    ('RECEPCION', 'APERTURA DE RECEPCION', 'ANTES DE ABRIR AL PUBLICO', 'DIARIA', '08:00', NULL, NULL),
    ('RECEPCION', 'CORTE DE CAJA', 'CIERRE DEL TURNO', 'DIARIA', '15:00', NULL, NULL)
) AS r (area, nombre, descripcion, frecuencia, inicio, fin, dia)
JOIN AREA a ON a.TX_NOMBRE = r.area
ON CONFLICT DO NOTHING;

INSERT INTO TAREA (FK_RUTINA, TX_NOMBRE, DN_ORDEN, DN_USUARIO_CREADOR)
SELECT ru.PK_ID, t.nombre, t.orden, 0
FROM (VALUES
    ('REVISION DE BANOS', 'Revisar papel, jabon y toallas', 1),
    ('REVISION DE BANOS', 'Limpiar lavabos y espejos', 2),
    ('REVISION DE BANOS', 'Trapear piso', 3),
    ('REVISION DE BANOS', 'Vaciar botes de basura', 4),
    ('REVISION DE EXTINTORES', 'Revisar que la aguja este en verde', 1),
    ('REVISION DE EXTINTORES', 'Revisar sello y pasador', 2),
    ('REVISION DE EXTINTORES', 'Confirmar fecha de recarga vigente', 3),
    ('APERTURA DE COCINA', 'Encender campana y verificar gas', 1),
    ('APERTURA DE COCINA', 'Revisar caducidades del refrigerador', 2),
    ('APERTURA DE COCINA', 'Desinfectar superficies de trabajo', 3),
    ('CONTROL DE TEMPERATURAS', 'Refrigeradores entre 1 y 4 grados', 1),
    ('CONTROL DE TEMPERATURAS', 'Congelador a -18 grados o menos', 2),
    ('CIERRE DE COCINA', 'Guardar y etiquetar alimentos', 1),
    ('CIERRE DE COCINA', 'Apagar gas y equipos', 2),
    ('CIERRE DE COCINA', 'Lavar y desinfectar utensilios', 3),
    ('APERTURA DE RECEPCION', 'Encender equipo y terminal de pago', 1),
    ('APERTURA DE RECEPCION', 'Revisar reservaciones del dia', 2),
    ('APERTURA DE RECEPCION', 'Limpiar mostrador', 3),
    ('CORTE DE CAJA', 'Contar efectivo', 1),
    ('CORTE DE CAJA', 'Imprimir corte de la terminal', 2)
) AS t (rutina, nombre, orden)
JOIN RUTINA ru ON ru.TX_NOMBRE = t.rutina AND ru.DN_ACTIVO
WHERE NOT EXISTS (SELECT 1 FROM TAREA x WHERE x.FK_RUTINA = ru.PK_ID AND x.TX_NOMBRE = t.nombre);

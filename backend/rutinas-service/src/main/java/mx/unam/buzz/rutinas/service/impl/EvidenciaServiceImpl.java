package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.EjecucionErrorCode;
import mx.unam.buzz.rutinas.common.constant.EvidenciaErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.ArchivoVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.EvidenciaVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.SubirEvidenciaRequestVO;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;
import mx.unam.buzz.rutinas.dominio.evidencia.Evidencia;
import mx.unam.buzz.rutinas.integration.almacenamiento.IAlmacenamientoArchivos;
import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.EvidenciaDO;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IEvidenciaRepository;
import mx.unam.buzz.rutinas.service.IEvidenciaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evidencias. Sube solo el operador que tomo la ronda (lo exige el dominio); las ve
 * cualquiera con acceso al area de la rutina, como la ronda misma.
 */
@Service
@RequiredArgsConstructor
public class EvidenciaServiceImpl implements IEvidenciaService {

    private final IEvidenciaRepository evidenciaRepository;
    private final IEjecucionRepository ejecucionRepository;
    private final IAlmacenamientoArchivos almacenamiento;
    private final ConsultaNombres nombres;

    @Override
    @Transactional
    public SingleResponseVO<EvidenciaVO> adjuntar(RequestVO<SubirEvidenciaRequestVO> request) {
        SubirEvidenciaRequestVO parameters = request.getParameters();
        UsuarioSesionVO sesion = request.getSesion();
        ValidationUtil.validate(parameters.contenido() == null || parameters.contenido().length == 0,
                EvidenciaErrorCode.ARCHIVO_OBLIGATORIO.getCode(), HttpStatus.BAD_REQUEST);
        EjecucionDO ronda = rondaConAcceso(parameters.idEjecucion(), sesion);

        Evidencia evidencia = Evidencia.adjuntar(ronda.getId(), EstadoEjecucion.valueOf(ronda.getEstado()),
                ronda.getIdOperador(), sesion.getId(), parameters.contenido(),
                StringUtils.left(parameters.nota(), 200),
                evidenciaRepository.countByIdEjecucionAndActivoTrue(ronda.getId()), LocalDateTime.now());
        String llave;
        try {
            llave = almacenamiento.guardar(parameters.contenido(), evidencia.getTipo().getExtension());
        } catch (UncheckedIOException ex) {
            throw new BusinessException(EvidenciaErrorCode.ALMACENAMIENTO.getCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        EvidenciaDO entity = new EvidenciaDO();
        entity.setIdEjecucion(evidencia.getIdEjecucion());
        entity.setIdSube(evidencia.getIdUsuario());
        entity.setTipoContenido(evidencia.getTipo().getTipoContenido());
        entity.setTamanoBytes(evidencia.getTamanoBytes());
        entity.setLlaveAlmacen(llave);
        entity.setNota(evidencia.getNota());
        entity.setFechaCaptura(evidencia.getFechaCaptura());
        evidenciaRepository.save(entity);
        return SingleResponseVO.ok(toVOs(List.of(entity)).getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<List<EvidenciaVO>> findByEjecucion(RequestVO<IdRequestVO> request) {
        EjecucionDO ronda = rondaConAcceso(request.getParameters().getId(), request.getSesion());
        return SingleResponseVO.ok(toVOs(
                evidenciaRepository.findByIdEjecucionAndActivoTrueOrderByFechaCapturaAsc(ronda.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoVO archivo(RequestVO<IdRequestVO> request) {
        EvidenciaDO entity = evidenciaRepository.findByIdAndActivoTrue(request.getParameters().getId())
                .orElseThrow(() -> new BusinessException(EvidenciaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
        rondaConAcceso(entity.getIdEjecucion(), request.getSesion());
        try {
            return new ArchivoVO(almacenamiento.leer(entity.getLlaveAlmacen()), entity.getTipoContenido());
        } catch (UncheckedIOException ex) {
            throw new BusinessException(EvidenciaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND);
        }
    }

    private EjecucionDO rondaConAcceso(Long idEjecucion, UsuarioSesionVO sesion) {
        EjecucionDO ronda = ejecucionRepository.findByIdAndActivoTrue(idEjecucion)
                .orElseThrow(() -> new BusinessException(EjecucionErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
        RutinaDO rutina = nombres.rutinas(List.of(ronda.getIdRutina())).get(ronda.getIdRutina());
        ValidationUtil.validate(rutina == null || !sesion.tieneArea(rutina.getIdArea()),
                EjecucionErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
        return ronda;
    }

    private List<EvidenciaVO> toVOs(List<EvidenciaDO> evidencias) {
        Map<Long, String> usuarios = nombres.usuarios(evidencias.stream().map(EvidenciaDO::getIdSube).toList());
        return evidencias.stream().map(e -> new EvidenciaVO(e.getId(), e.getIdEjecucion(), e.getTipoContenido(),
                e.getTamanoBytes(), e.getNota(), e.getFechaCaptura(), usuarios.get(e.getIdSube()),
                "/evidencias/" + e.getId() + "/archivo")).toList();
    }
}

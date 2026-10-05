package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.AreaConstant;
import mx.unam.buzz.rutinas.common.constant.AreaErrorCode;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.AreaResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.CreateAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.FindListAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.UpdateAreaRequestVO;
import mx.unam.buzz.rutinas.modelo.entity.AreaDO;
import mx.unam.buzz.rutinas.persistence.IAreaRepository;
import mx.unam.buzz.rutinas.service.IAreaService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio del modulo de areas.
 */
@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements IAreaService {

    private final IAreaRepository areaRepository;
    private final ModelMapper mapper;

    @Override
    @Transactional
    public SingleResponseVO<AreaResponseVO> create(RequestVO<CreateAreaRequestVO> request) {
        //Validar y normalizar la entrada
        ValidationUtil.validate(request.getParameters() == null,
                CommonErrorCode.PARAMETERS_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        CreateAreaRequestVO parameters = request.getParameters();
        String nombre = validateNombre(parameters.getNombre());
        ValidationUtil.validate(areaRepository.existsByNombreAndActivoTrue(nombre),
                AreaErrorCode.ALREADY_EXISTS.getCode(), HttpStatus.CONFLICT);

        //Guardar
        AreaDO entity = new AreaDO();
        entity.setNombre(nombre);
        entity.setDescripcion(ValidationUtil.normalize(parameters.getDescripcion(), AreaConstant.MAX_SIZE_DESCRIPCION));
        areaRepository.save(entity);

        return SingleResponseVO.ok(mapper.map(entity, AreaResponseVO.class));
    }

    @Override
    @Transactional
    public SingleResponseVO<Boolean> update(RequestVO<UpdateAreaRequestVO> request) {
        ValidationUtil.validate(request.getParameters() == null,
                CommonErrorCode.PARAMETERS_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        UpdateAreaRequestVO parameters = request.getParameters();
        AreaDO entity = findActive(parameters.getId());

        String nombre = validateNombre(parameters.getNombre());
        ValidationUtil.validate(areaRepository.existsByNombreAndActivoTrueAndIdNot(nombre, entity.getId()),
                AreaErrorCode.ALREADY_EXISTS.getCode(), HttpStatus.CONFLICT);

        entity.setNombre(nombre);
        entity.setDescripcion(ValidationUtil.normalize(parameters.getDescripcion(), AreaConstant.MAX_SIZE_DESCRIPCION));
        areaRepository.save(entity);

        return SingleResponseVO.ok(Boolean.TRUE);
    }

    @Override
    @Transactional
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        //Borrado logico: el registro se conserva para la trazabilidad
        AreaDO entity = findActive(request.getParameters().getId());
        entity.setActivo(Boolean.FALSE);
        areaRepository.save(entity);

        return SingleResponseVO.ok(Boolean.TRUE);
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<AreaResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        AreaDO entity = findActive(request.getParameters().getId());
        return SingleResponseVO.ok(mapper.map(entity, AreaResponseVO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<AreaResponseVO> findList(RequestVO<FindListAreaRequestVO> request) {
        FindListAreaRequestVO filters = request.getParameters() != null
                ? request.getParameters() : new FindListAreaRequestVO();
        ValidationUtil.validate(!AreaConstant.ORDER_FIELDS.contains(request.getOrderBy()),
                CommonErrorCode.INVALID_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
        Sort sort = Sort.by(Sort.Direction.fromOptionalString(request.getOrderType()).orElse(Sort.Direction.ASC),
                request.getOrderBy());

        Page<AreaDO> page = areaRepository.findList(
                ValidationUtil.normalize(filters.getNombre(), AreaConstant.MAX_SIZE_NOMBRE),
                PageRequest.of(request.getPage(), request.getSize(), sort));

        List<AreaResponseVO> data = page.getContent().stream()
                .map(entity -> mapper.map(entity, AreaResponseVO.class))
                .toList();
        return ResponseVO.of(page, data);
    }

    private AreaDO findActive(Long id) {
        ValidationUtil.validate(ValidationUtil.isNullOrZero(id),
                CommonErrorCode.ID_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        return areaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException(
                        AreaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
    }

    private String validateNombre(String value) {
        String nombre = ValidationUtil.normalize(value, AreaConstant.MAX_SIZE_NOMBRE);
        ValidationUtil.validate(nombre == null, AreaErrorCode.NAME_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        return nombre;
    }
}

package gestion_comercial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grupos_catalogo")
public class GrupoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo_catalogo")
    private Integer idGrupoCatalogo;

    @Column(
            name = "nombre_visible",
            nullable = false,
            length = 120
    )
    private String nombreVisible;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(
            name = "imagen_url",
            length = 255
    )
    private String imagenUrl;

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    public GrupoCatalogo() {
    }


    public Integer getIdGrupoCatalogo() {
        return idGrupoCatalogo;
    }


    public void setIdGrupoCatalogo(
            Integer idGrupoCatalogo
    ) {
        this.idGrupoCatalogo =
                idGrupoCatalogo;
    }


    public String getNombreVisible() {
        return nombreVisible;
    }


    public void setNombreVisible(
            String nombreVisible
    ) {
        this.nombreVisible =
                nombreVisible;
    }


    public String getDescripcion() {
        return descripcion;
    }


    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion =
                descripcion;
    }


    public String getImagenUrl() {
        return imagenUrl;
    }


    public void setImagenUrl(
            String imagenUrl
    ) {
        this.imagenUrl =
                imagenUrl;
    }


    public Boolean getActivo() {
        return activo;
    }


    public void setActivo(
            Boolean activo
    ) {
        this.activo =
                activo;
    }
}
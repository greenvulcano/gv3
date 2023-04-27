/**
 *
 */
package it.greenvulcano.gvesb.virtual.chart.generator.encontrack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeTableXYDataset;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.chart.generator.ChartGenerator;
import it.greenvulcano.util.txt.DateUtils;

/**
 * @author gianluca
 *
 */
public abstract class BaseGenerator implements ChartGenerator {
    protected static Map<String, String> eventLabel = new HashMap<String, String>();
    protected static Map<String, String> gwtEventLabel = new HashMap<String, String>();
    protected Locale loc = Locale.forLanguageTag("es_MT");
    protected TimeZone tz = TimeZone.getDefault();
    protected String type;
    protected int[] width = new int[] {-1};
    protected int[] height = new int[] {-1};
    static {
        eventLabel.put("1", "Anomalía en el consumo de combustible");
        eventLabel.put("2", "Carga de combustible en estación no autorizada");
        eventLabel.put("3", "Uso Restringido");
        eventLabel.put("4", "Entrada a geocerca ");
        eventLabel.put("5", "Salida a geocerca");
        eventLabel.put("6", "Entrada y salida de geocerca");
        eventLabel.put("7", "Cruce de geocerca");
        eventLabel.put("8", "Parada autorizada");
        eventLabel.put("9", "Tiempo de estadía excedido");
        eventLabel.put("10", "Ralentí o apagado en geocerca");
        eventLabel.put("11", "Cruce de PDI");
        eventLabel.put("12", "Tolerancia");
        eventLabel.put("13", "Incumplimiento de ruta");
        eventLabel.put("14", "Incumplimiento de cronoruta");
        eventLabel.put("15", "Tolerancia tiempo");
        eventLabel.put("16", "Tolerancia distancia");
        eventLabel.put("20", "Vencimiento de licencia");
        eventLabel.put("21", "Vencimiento de seguro");
        eventLabel.put("22", "Exceso de velocidad leve");
        eventLabel.put("23", "Exceso de velocidad medio");
        eventLabel.put("24", "Exceso de velocidad grave");
        eventLabel.put("25", "Conducción continua");
        eventLabel.put("31", "Vehículo sin posición");
        eventLabel.put("32", "Batería baja");
        eventLabel.put("33", "Batería desconectada");
        eventLabel.put("34", "Arnés y batería desconectados");
        eventLabel.put("35", "Arnés desconectado");
        eventLabel.put("36", "Remolque y conducción continua");

        gwtEventLabel.put("1", "Exceso de velocidad");
        gwtEventLabel.put("2", "Aceleración brusca");
        gwtEventLabel.put("3", "Conducción nocturna");
        gwtEventLabel.put("4", "Frenado brusco");
        gwtEventLabel.put("5", "Viaje largo");
        gwtEventLabel.put("6", "Tiempo inactivo");
        gwtEventLabel.put("7", "Final del tiempo inactivo");
        gwtEventLabel.put("8", "Geocerca dentro");
        gwtEventLabel.put("9", "Geocerca fuera");
        gwtEventLabel.put("10", "Uso Restringido");
        gwtEventLabel.put("11", "Tomando esquinas");
        gwtEventLabel.put("12", "Cambio rápido de carril");
        gwtEventLabel.put("18", "Geocerca regional dentro");
        gwtEventLabel.put("19", "Geocerca regional fuera");
        gwtEventLabel.put("20", "Manejo duro");
        gwtEventLabel.put("21", "Comportamiento desconocido");
        gwtEventLabel.put("28", "PDI dentro");
        gwtEventLabel.put("29", "PDI fuera");
        gwtEventLabel.put("131", "Desconexión batería");
        gwtEventLabel.put("132", "Batería baja");
        gwtEventLabel.put("135", "Desajuste de VIN");
        gwtEventLabel.put("136", "Evento de conexión");
        gwtEventLabel.put("137", "Evento de desconexión");
        gwtEventLabel.put("138", "Primer evento de conexión");
        gwtEventLabel.put("144", "Batería crítica");
        gwtEventLabel.put("145", "Apagado de batería");
        gwtEventLabel.put("146", "Batería normal");
        gwtEventLabel.put("147", "Etiqueta de batería baja");
        gwtEventLabel.put("155", "Lista de DTC del vehículo calificado");
        gwtEventLabel.put("156", "Lista de DTC pendientes del vehículo");
        gwtEventLabel.put("237", "Heartbeat del dispositivo");
        gwtEventLabel.put("238", "Mover encendido apagado");
        gwtEventLabel.put("239", "Viaje de largo tiempo de longitud cero");
        gwtEventLabel.put("240", "Mover sin GPS");
        gwtEventLabel.put("241", "Fallo del acelerómetro");
        gwtEventLabel.put("244", "Manipulación del dispositivo");
        gwtEventLabel.put("245", "Refrigerante del motor");
        gwtEventLabel.put("246", "ID del heartbeat del dispositivo");
        gwtEventLabel.put("247", "ID del dispositivo sin comunicación");
        gwtEventLabel.put("248", "ID del Dispositivo sin activación");
        gwtEventLabel.put("249", "Conducción distraída");
        gwtEventLabel.put("250", "Repostar");
        gwtEventLabel.put("900", "Accidente de tráfico");
        gwtEventLabel.put("997", "Heartbeat del viaje Eld");
        gwtEventLabel.put("998", "Inicio del viaje");
        gwtEventLabel.put("999", "Fin del viaje");

        gwtEventLabel.put("speeding", gwtEventLabel.get("1"));
        gwtEventLabel.put("acceleration", gwtEventLabel.get("2"));
        gwtEventLabel.put("braking", gwtEventLabel.get("4"));
    }

    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            this.type = XMLConfig.get(node, "@type");
            this.width = Arrays.stream(XMLConfig.get(node, "@width", "-1").split(",")).mapToInt(Integer::parseInt).toArray();
            this.height = Arrays.stream(XMLConfig.get(node, "@height", "-1").split(",")).mapToInt(Integer::parseInt).toArray();
            getLogger().debug("ChartGenerator[ " + this.type + "] configured");
        }
        catch (XMLConfigException exc) {
            getLogger().error("An error occurred while reading configuration", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        catch (Exception exc) {
            getLogger().error("A generic error occurred while initializing", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public int[] getPreferredHeight() {
        return this.height;
    }

    @Override
    public int[] getPreferredWidth() {
        return this.width;
    }

    protected long getDelta(String aggrType) {
        switch (aggrType) {
            case "H":
                return 60 * 60 * 1000;
            case "D":
                return 24 * 60 * 60 * 1000;
            case "W":
                return 24 * 60 * 60 * 1000;
            case "M":
                return 30 * 24 * 60 * 60 * 1000;
            case "Y":
                return 365 * 24 * 60 * 60 * 1000;
        }
        return 24 * 60 * 1000;
    }

    protected void addTSentry(TimeSeries ts, String aggrType, String d, float v) throws Exception {
        if ("H".equals(aggrType)) {
            ts.add(new Hour(DateUtils.stringToDate(d, "yyyy-MM-dd HH"), this.tz, this.loc), v);
        }
        else if ("D".equals(aggrType)) {
            ts.add(new Day(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v);
        }
        else if ("W".equals(aggrType)) {
            //ts.add(new Week(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v);
            ts.add(new Day(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v);
        }
        else if ("M".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy-MM"), this.tz, this.loc), v);
        }
        else if ("Y".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy"), this.tz, this.loc), v);
        }
        else {
            throw new Exception("Invalid aggregation type [" + aggrType + "]");
        }
    }

    protected void addTSentry(TimeTableXYDataset ts, String aggrType, String d, float v, String serieName) throws Exception {
        if ("H".equals(aggrType)) {
            ts.add(new Hour(DateUtils.stringToDate(d, "yyyy-MM-dd HH"), this.tz, this.loc), v, serieName);
        }
        else if ("D".equals(aggrType)) {
            ts.add(new Day(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v, serieName);
        }
        else if ("W".equals(aggrType)) {
            ts.add(new Day(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v, serieName);
            //ts.add(new Week(DateUtils.stringToDate(d, "yyyy-MM-dd"), this.tz, this.loc), v, serieName);
        }
        else if ("M".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy-MM"), this.tz, this.loc), v, serieName);
        }
        else if ("Y".equals(aggrType)) {
            ts.add(new Month(DateUtils.stringToDate(d, "yyyy"), this.tz, this.loc), v, serieName);
        }
        else {
            throw new Exception("Invalid aggregation type [" + aggrType + "]");
        }
    }
}

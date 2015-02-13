/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

/**
 * @author      Danilo Iannaccone
 *
 */
public class DocumentDescriptor {
    /* Document name                                       */
    private String   name;

    /**
     * Document group.
     * Empty string for the default group.
     */
    private String   group;

    /* Document description                                */
    private String   description;

    /* Document label                                      */
    private String   label;

    /* Document roles enabled to R\W                       */
    private String[] readWriteRoles;

    /* Document roles enabled to Read Only                 */
    private String[] readOnlyRoles;

    /* Document External System roles enabled to Read Only */
    private String[] externalSystemRoles;

    /* DocumentDescriptor Constructors                     */
    public DocumentDescriptor() {
    }

    /* DocumentDescriptor Constructors                     */
    public DocumentDescriptor(String name, String description, String group, String label, String[] readWriteRoles,
            String[] readOnlyRoles, String[] externalSystemRoles) {
        this.name = name;
        this.description = description;
        this.label = label;
        this.readWriteRoles = readWriteRoles;
        this.readOnlyRoles = readOnlyRoles;
        this.externalSystemRoles = externalSystemRoles;
        if (group == null) {
            group = "";
        }
        this.group = group;
    }

    /**
     * Getter method to find the name of the document.
     *
     * @return the name of the document.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method to find the description of the document.
     *
     * @return the description of the document.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method to find the label of the document.
     *
     * @return the label of the document.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Getter method to find all the users of reading and writing
     *
     * @return the reading/writing  roles.
     */
    public String[] getReadWriteRoles() {
        return readWriteRoles;
    }

    /**
     * Getter method to find all the users of only reading
     *
     * @return the reading roles.
     */
    public String[] getReadOnlyRoles() {
        return readOnlyRoles;
    }

    /**
     * Getter method to find all the users of the exsternal systems
     *
     * @return the users of the exsternal systems.
     */
    public String[] getExternalSystemRoles() {
        return externalSystemRoles;
    }

    /**
     * @return the group of the document
     */
    public String getGroup() {
        return group;
    }
}
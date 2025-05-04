package com.kerware.reusine.simualteur;

import com.kerware.simulateur.SituationFamiliale;

/**
 *  Cette classe permet de simuler le calcul de l'impôt sur le revenu
 *  en France pour l'année 2024 sur les revenus de l'année 2023 pour
 *  des cas simples de contribuables célibataires, mariés, divorcés, veufs
 *  ou pacsés avec ou sans enfants à charge ou enfants en situation de handicap
 *  et parent isolé.
 * <p>
 *  EXEMPLE DE CODE DE TRES MAUVAISE QUALITE FAIT PAR UN DEBUTANT
 * <p>
 *  Pas de lisibilité, pas de commentaires, pas de tests
 *  Pas de documentation, pas de gestion des erreurs
 *  Pas de logique métier, pas de modularité
 *  Pas de gestion des exceptions, pas de gestion des logs
 *  Principe "Single Responsability" non respecté
 *  Pas de traçabilité vers les exigences métier
 * <p>
 *  Pourtant ce code fonctionne correctement
 *  Il s'agit d'un "legacy" code qui est difficile à maintenir
 *  L'auteur n'a pas fourni de tests unitaires
 **/

public class SimulateurReusine {
    // Les limites des tranches de revenus imposables
    private final int[] limitTranchRevImposab = {
            0, 11294, 28797, 82341, 177106, Integer.MAX_VALUE
    };

    // Les taux d'imposition par tranche
    private final double[] tauxImpositionParTranche = {
            0.0, 0.11, 0.3, 0.41, 0.45
    };

    // Les limites des tranches pour la contribution
    // exceptionnelle sur les hauts revenus
    private final int[] limitesTranchCEHR = {
            0, 250000, 500000, 1000000, Integer.MAX_VALUE
    };

    // Les taux de la contribution exceptionnelle
    // sur les hauts revenus pour les celibataires
    private final double[] tauxCEHRCelibataire = {
            0.0, 0.03, 0.04, 0.04
    };

    // Les taux de la contribution exceptionnelle
    // sur les hauts revenus pour les couples
    private final double[] tauxCEHRCouple = {
            0.0, 0.0, 0.03, 0.04
    };

    private static final double TAUX_ABATTEMENT = 0.1;
    private static final int LIMITE_ABATTEMENT_MIN = 495;
    private static final int LIMITE_ABATTEMENT_MAX = 14171;

    private static final double PLAFOND_BAISSE_MAX_DEMI_PART = 1759;

    private static final double SEUIL_DECOTE_DECLARANT_SEUL = 1929;
    private static final double SEUIL_DECOTE_DECLARANT_COUPLE = 3191;
    private static final double DECOTE_MAX_DECLARANT_SEUL = 873;
    private static final double DECOTE_MAX_DECLARANT_COUPLE = 1444;
    private static final double TAUX_DECOTE = 0.4525;

    private static final int REPETITION_CALCUL = 5;
    private static final double DEMI_PART = 0.5;

    // revenu net
    private int revenuNetDeclarant1;
    private int revenuNetDeclarant2;
    // nb enfants
    private int nombreEnfants;
    // nb enfants handicapés
    private int nombreEnfantsHandicapes;

    // revenu fiscal de référence
    private double revenuFiscalReference;

    // revenu imposable
    private double revenuImposable;

    // abattement
    private double abattement;

    // nombre de parts des  déclarants
    private double nombrePartsDeclarant;
    // nombre de parts du foyer fiscal
    private double nombrePartsFoyerFiscal;

    // decote
    private double decote;
    // impôt des déclarants
    private double impotDeclarant;
    // impôt du foyer fiscal
    private double impotFoyerFiscal;
    private double impotAvantDecote;
    // parent isolé
    private boolean parentIsole;
    // Contribution exceptionnelle sur les hauts revenus
    private double contributExceptHautsRev;

    // Getters pour adapter le code legacy pour les tests unitaires
    public final double getRevenuReference() {
        return revenuFiscalReference;
    }

    public final double getDecote() {
        return decote;
    }

    public final double getAbattement() {
        return abattement;
    }

    public final double getNbParts() {
        return nombrePartsFoyerFiscal;
    }

    public final double getImpotAvantDecote() {
        return impotAvantDecote;
    }

    public final double getImpotNet() {
        return impotFoyerFiscal;
    }

    public final int getRevenuNetDeclatant1() {
        return revenuNetDeclarant1;
    }

    public final int getRevenuNetDeclatant2() {
        return revenuNetDeclarant2;
    }

    public final double getContribExceptionnelle() {
        return contributExceptHautsRev;
    }

    /***
     * Initialisation des variables
     *
     * @param revNetDeclarant1 revenu net declarant 1
     * @param revNetDeclarant2 revenu net declarant 2
     * @param situatFamiliale situation familiale
     * @param nbEnfants nombre d'enfants
     * @param nbEnfantsHandicapes nombre d'enfants handicapés
     * @param parentSeul parent isolé
     */
    private void definirParametresCalculImpot(
            int revNetDeclarant1,
            int revNetDeclarant2,
            SituationFamiliale situatFamiliale,
            int nbEnfants,
            int nbEnfantsHandicapes,
            boolean parentSeul
    ) {
        this.revenuNetDeclarant1 = revNetDeclarant1;
        this.revenuNetDeclarant2 = revNetDeclarant2;

        this.nombreEnfants = nbEnfants;
        this.nombreEnfantsHandicapes = nbEnfantsHandicapes;
        this.parentIsole = parentSeul;

        System.out.println("--------------------------------------------");
        System.out.println( "Revenu net declarant1 : " +
                this.revenuNetDeclarant1);
        System.out.println( "Revenu net declarant2 : " +
                this.revenuNetDeclarant2);
        System.out.println( "Situation familiale : " +
                situatFamiliale.name() );
    }

    /***
     * Fonction de calcul de l'impôt sur le revenu net
     * en France en 2024 sur les revenu 2023
     *
     * @param revNetDeclarant1 revenu net declarant 1
     * @param revNetDeclarant2 revenu net declarant 2
     * @param situatFamiliale situation familiale
     * @param nbEnfants nombre d'enfants
     * @param nbEnfantsHandicapes nombre d'enfants handicapés
     * @param parentSeul parent isolé
     */

    public int calculImpot(
            int revNetDeclarant1,
            int revNetDeclarant2,
            SituationFamiliale situatFamiliale,
            int nbEnfants,
            int nbEnfantsHandicapes,
            boolean parentSeul
    ) {

        try{
            // Préconditions
            SimulateurValidateur.validiteParametresCalCulImpot(
                    revNetDeclarant1,
                    revNetDeclarant2,
                    situatFamiliale,
                    nbEnfants,
                    nbEnfantsHandicapes,
                    parentSeul
            );

            // Initialisation des variables
            definirParametresCalculImpot(
                    revNetDeclarant1,
                    revNetDeclarant2,
                    situatFamiliale,
                    nbEnfants,
                    nbEnfantsHandicapes,
                    parentSeul
            );

            // Abattement
            // EXIGENCE : EXG_IMPOT_02
            calculAbattement(situatFamiliale);

            // parts déclarants
            // EXIG  : EXG_IMPOT_03
            calculPartsDeclarants(situatFamiliale);

            // EXIGENCE : EXG_IMPOT_07:
            // Contribution exceptionnelle sur les hauts revenus
            calculContributionsExceptionnellesHautsRevenus();

            // Calcul impôt des declarants
            // EXIGENCE : EXG_IMPOT_04
            calCulImpotDeclarant();

            // Calcul impôt foyer fiscal complet
            // EXIGENCE : EXG_IMPOT_04
            calculImpotFoyerFiscalComplet();

            // Vérification de la baisse d'impôt autorisée
            // EXIGENCE : EXG_IMPOT_05
            // baisse impot
            verificationBaisseImpot();

            // Calcul de la decote
            // EXIGENCE : EXG_IMPOT_06
            calculDecote();

            return (int) impotFoyerFiscal;
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
            throw exception;
        }
    }

    /***
     * Calcul de l'abattement
     *
     * @param situationFamiliale situation familiale
     */
    private void calculAbattement(SituationFamiliale situationFamiliale){
        long abt1 = Math.round(revenuNetDeclarant1 * TAUX_ABATTEMENT);
        long abt2 = Math.round(revenuNetDeclarant2 * TAUX_ABATTEMENT);

        // Abattement
        if (abt1 > LIMITE_ABATTEMENT_MAX) {
            abt1 = LIMITE_ABATTEMENT_MAX;
        }
        if ( situationFamiliale == SituationFamiliale.MARIE
                || situationFamiliale == SituationFamiliale.PACSE ) {
            if (abt2 > LIMITE_ABATTEMENT_MAX) {
                abt2 = LIMITE_ABATTEMENT_MAX;
            }
        }

        if (abt1 < LIMITE_ABATTEMENT_MIN) {
            abt1 = LIMITE_ABATTEMENT_MIN;
        }

        if ( situationFamiliale == SituationFamiliale.MARIE
                || situationFamiliale == SituationFamiliale.PACSE ) {
            if (abt2 < LIMITE_ABATTEMENT_MIN) {
                abt2 = LIMITE_ABATTEMENT_MIN;
            }
        }

        abattement = abt1 + abt2;
        System.out.println( "Abattement : " + abattement);

        revenuFiscalReference = revenuNetDeclarant1 + revenuNetDeclarant2;
        revenuFiscalReference -= abattement;
        if ( revenuFiscalReference < 0 ) {
            revenuFiscalReference = 0;
        }

        System.out.println(
                "Revenu fiscal de référence : " + revenuFiscalReference
        );
    }

    /***
     * Calcul des parts des déclarants
     *
     * @param situationFamiliale situation familiale
     */
    private void calculPartsDeclarants(SituationFamiliale situationFamiliale){
        switch ( situationFamiliale ) {
            case CELIBATAIRE, DIVORCE, VEUF:
                nombrePartsDeclarant = 1;
                break;
            case MARIE, PACSE:
                nombrePartsDeclarant = 2;
                break;
            default:
                break;
        }

        System.out.println(
                "Nombre d'enfants  : " + this.nombreEnfants
        );
        System.out.println(
                "Nombre d'enfants handicapés : " + this.nombreEnfantsHandicapes
        );

        // parts enfants à charge
        if ( this.nombreEnfants <= 2 ) {
            nombrePartsFoyerFiscal =
                    nombrePartsDeclarant + this.nombreEnfants * DEMI_PART;
        } else {
            nombrePartsFoyerFiscal =
                    nombrePartsDeclarant +  1.0 + ( this.nombreEnfants - 2 );
        }

        // parent isolé

        System.out.println( "Parent isolé : " + this.parentIsole);

        if (this.parentIsole) {
            if ( this.nombreEnfants > 0 ){
                nombrePartsFoyerFiscal = nombrePartsFoyerFiscal + DEMI_PART;
            }
        }

        // Veuf avec enfant
        if ( situationFamiliale == SituationFamiliale.VEUF
                && this.nombreEnfants > 0 ) {
            nombrePartsFoyerFiscal = nombrePartsFoyerFiscal + 1;
        }

        // enfant handicapé
        nombrePartsFoyerFiscal =
                nombrePartsFoyerFiscal
                        + this.nombreEnfantsHandicapes * DEMI_PART;

        System.out.println( "Nombre de parts : " + nombrePartsFoyerFiscal);
    }

    /***
     * Calcul des Contributions Exceptionnelles pour les Hauts Revenus
     */
    private void calculContributionsExceptionnellesHautsRevenus(){
        contributExceptHautsRev = 0;
        for(int i = 0; i < REPETITION_CALCUL; i++){
            if ( revenuFiscalReference >= limitesTranchCEHR[i]
                    && revenuFiscalReference < limitesTranchCEHR[i+1] ) {
                double somme = revenuFiscalReference - limitesTranchCEHR[i];
                if ( nombrePartsDeclarant == 1 ) {
                    contributExceptHautsRev += somme * tauxCEHRCelibataire[i];
                } else {
                    contributExceptHautsRev += somme * tauxCEHRCouple[i];
                }
                break;
            } else {
                double somme = limitesTranchCEHR[i+1] - limitesTranchCEHR[i];
                if ( nombrePartsDeclarant == 1 ) {
                    contributExceptHautsRev += somme * tauxCEHRCelibataire[i];
                } else {
                    contributExceptHautsRev += somme * tauxCEHRCouple[i];
                }
            }
        }

        contributExceptHautsRev = Math.round(contributExceptHautsRev);
        System.out.println(
                "Contribution exceptionnelle sur les hauts revenus : "
                        + contributExceptHautsRev
        );
    }

    /***
     * Calcul de l'impot du déclarant
     */
    private void calCulImpotDeclarant(){
        revenuImposable = revenuFiscalReference / nombrePartsDeclarant;

        impotDeclarant = 0;

        for(int i = 0; i < REPETITION_CALCUL; i++){
            if ( revenuImposable >= limitTranchRevImposab[i]
                    && revenuImposable < limitTranchRevImposab[i+1] ) {
                impotDeclarant += tauxImpositionParTranche[i] *
                        ( revenuImposable - limitTranchRevImposab[i] );
                break;
            } else {
                impotDeclarant += tauxImpositionParTranche[i] *
                    ( limitTranchRevImposab[i+1] - limitTranchRevImposab[i] );
            }
        }

        impotDeclarant = impotDeclarant * nombrePartsDeclarant;
        impotDeclarant = Math.round(impotDeclarant);

        System.out.println( "Impôt brut des déclarants : " + impotDeclarant);
    }

    /***
     * Calcul de l'impot d'un foyer fiscal complet
     */
    private void calculImpotFoyerFiscalComplet(){
        revenuImposable =  revenuFiscalReference / nombrePartsFoyerFiscal;
        impotFoyerFiscal = 0;

        for(int i = 0; i < REPETITION_CALCUL; i++){
            if ( revenuImposable >= limitTranchRevImposab[i]
                    && revenuImposable < limitTranchRevImposab[i+1] ) {
                impotFoyerFiscal += tauxImpositionParTranche[i] *
                        ( revenuImposable - limitTranchRevImposab[i] );
                break;
            } else {
                impotFoyerFiscal += tauxImpositionParTranche[i] *
                        (limitTranchRevImposab[i+1] - limitTranchRevImposab[i]);
            }
        }

        impotFoyerFiscal = impotFoyerFiscal * nombrePartsFoyerFiscal;
        impotFoyerFiscal = Math.round(impotFoyerFiscal);

        System.out.println(
                "Impôt brut du foyer fiscal complet : " + impotFoyerFiscal);
    }

    /***
     * Vérfie la baisse des impots
     */
    private void verificationBaisseImpot(){
        // Plafond de baisse maximal par demi part
        double baisseImpot = impotDeclarant - impotFoyerFiscal;

        System.out.println( "Baisse d'impôt : " + baisseImpot );

        // dépassement plafond
        double ecartPts = nombrePartsFoyerFiscal - nombrePartsDeclarant;

        double plafond = (ecartPts / DEMI_PART) * PLAFOND_BAISSE_MAX_DEMI_PART;

        System.out.println( "Plafond de baisse autorisée " + plafond );

        if ( baisseImpot >= plafond ) {
            impotFoyerFiscal = impotDeclarant - plafond;
        }

        System.out.println(
                "Impôt brut après plafonnement avant decote : "
                        + impotFoyerFiscal
        );
        impotAvantDecote = impotFoyerFiscal;
    }

    /***
     * Calcul de la décote
     */
    private void calculDecote(){
        decote = 0;

        // decote
        if ( nombrePartsDeclarant == 1 ) {
            if ( impotFoyerFiscal < SEUIL_DECOTE_DECLARANT_SEUL) {
                decote = DECOTE_MAX_DECLARANT_SEUL -
                        ( impotFoyerFiscal * TAUX_DECOTE);
            }
        }
        if (  nombrePartsDeclarant == 2 ) {
            if ( impotFoyerFiscal < SEUIL_DECOTE_DECLARANT_COUPLE) {
                decote =  DECOTE_MAX_DECLARANT_COUPLE -
                        ( impotFoyerFiscal * TAUX_DECOTE);
            }
        }
        decote = Math.round( decote );

        if ( impotFoyerFiscal <= decote ) {
            decote = impotFoyerFiscal;
        }

        System.out.println( "Decote : " + decote );

        impotFoyerFiscal = impotFoyerFiscal - decote;

        impotFoyerFiscal += contributExceptHautsRev;

        impotFoyerFiscal = Math.round(impotFoyerFiscal);

        System.out.println(
                "Impôt sur le revenu net final : " + impotFoyerFiscal
        );
    }
}
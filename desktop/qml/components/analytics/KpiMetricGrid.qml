import QtQuick
import QtQuick.Controls

Item {
    id: root

    implicitWidth: 1000
    implicitHeight: 110

    readonly property real cardWidth: (width - 14 * 3) / 4

    Row {
        anchors.fill: parent
        spacing: 14

        // Card 1: Statutory SLA Compliance
        Rectangle {
            width: root.cardWidth
            height: parent.height
            radius: 10
            color: "#FFFFFF"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 14
                spacing: 4

                Row {
                    width: parent.width
                    Text { text: "🎯 SLA COMPLIANCE"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B" }
                    Item { width: parent.width - 160; height: 1 }
                    Rectangle {
                        width: 58
                        height: 18
                        radius: 9
                        color: "#DCFCE7"
                        Text { anchors.centerIn: parent; text: "ON TARGET"; font.pixelSize: 8; font.weight: Font.Bold; color: "#15803D" }
                    }
                }

                Text {
                    text: analyticsController.overallSlaCompliance.toFixed(1) + "%"
                    font.pixelSize: 22
                    font.weight: Font.Bold
                    color: "#0F172A"
                }

                Text {
                    text: "↑ +1.8% vs last cycle (Statutory Mandate: 90%)"
                    font.pixelSize: 9
                    color: "#16A34A"
                }
            }
        }

        // Card 2: Average Turnaround Time (TAT)
        Rectangle {
            width: root.cardWidth
            height: parent.height
            radius: 10
            color: "#FFFFFF"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 14
                spacing: 4

                Row {
                    width: parent.width
                    Text { text: "⏱ AVG TURNAROUND"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B" }
                    Item { width: parent.width - 160; height: 1 }
                    Rectangle {
                        width: 52
                        height: 18
                        radius: 9
                        color: "#EEF2FF"
                        Text { anchors.centerIn: parent; text: "OPTIMAL"; font.pixelSize: 8; font.weight: Font.Bold; color: "#4338CA" }
                    }
                }

                Text {
                    text: analyticsController.avgTurnaroundHours.toFixed(1) + " Hours"
                    font.pixelSize: 22
                    font.weight: Font.Bold
                    color: "#0F172A"
                }

                Text {
                    text: "↓ 3.2 hours faster from mobile submission"
                    font.pixelSize: 9
                    color: "#4338CA"
                }
            }
        }

        // Card 3: Citizen CSAT Rating
        Rectangle {
            width: root.cardWidth
            height: parent.height
            radius: 10
            color: "#FFFFFF"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 14
                spacing: 4

                Row {
                    width: parent.width
                    Text { text: "⭐ CITIZEN CSAT"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B" }
                    Item { width: parent.width - 160; height: 1 }
                    Rectangle {
                        width: 56
                        height: 18
                        radius: 9
                        color: "#FEF3C7"
                        Text { anchors.centerIn: parent; text: "4.7 / 5.0"; font.pixelSize: 8; font.weight: Font.Bold; color: "#B45309" }
                    }
                }

                Row {
                    spacing: 6
                    Text {
                        text: analyticsController.csatRating.toFixed(2)
                        font.pixelSize: 22
                        font.weight: Font.Bold
                        color: "#0F172A"
                    }
                    Text {
                        text: "★★★★★"
                        font.pixelSize: 14
                        color: "#F59E0B"
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }

                Text {
                    text: "Based on 1,240 post-resolution mobile sign-offs"
                    font.pixelSize: 9
                    color: "#64748B"
                }
            }
        }

        // Card 4: Municipal Budget & Unit Efficiency
        Rectangle {
            width: root.cardWidth
            height: parent.height
            radius: 10
            color: "#FFFFFF"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 14
                spacing: 4

                Row {
                    width: parent.width
                    Text { text: "💰 BUDGET UTILIZED"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B" }
                    Item { width: parent.width - 160; height: 1 }
                    Rectangle {
                        width: 58
                        height: 18
                        radius: 9
                        color: "#F0FDF4"
                        Text { anchors.centerIn: parent; text: "AUDITED"; font.pixelSize: 8; font.weight: Font.Bold; color: "#166534" }
                    }
                }

                Text {
                    text: analyticsController.budgetUtilizedFormatted
                    font.pixelSize: 22
                    font.weight: Font.Bold
                    color: "#0F172A"
                }

                Text {
                    text: "Unit efficiency: " + analyticsController.costPerResolutionFormatted + " per resolved grievance"
                    font.pixelSize: 9
                    color: "#64748B"
                }
            }
        }
    }
}
